import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Structural RTM/documentation validator. It deliberately does not infer
 * functional compliance from class names: PASS rows also need local evidence,
 * a real test source, and a current Surefire report with no failures/errors/
 * skipped tests.
 */
public final class ValidateRtm {
    private static final Pattern LINK = Pattern.compile("\\[[^\\]]+\\]\\(([^)]+)\\)");
    private static final Pattern REQUIREMENT_ID = Pattern.compile("(?:RF|RNF|RC)-\\d{2}");
    private static final Pattern ATTRIBUTE = Pattern.compile("\\b%s=\"(\\d+)\"");
    private static final Pattern EXTERNAL_PENDING =
            Pattern.compile("PENDENTE EXTERNO:\\s*([^;|]+)", Pattern.CASE_INSENSITIVE);

    private static int errors;
    private static int checkedLinks;
    private static int checkedRequirements;
    private static final List<String> externalPending = new ArrayList<>();

    private ValidateRtm() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("").toAbsolutePath().normalize();
        List<Path> markdownFiles = findMarkdownFiles(root);
        for (Path markdown : markdownFiles) {
            validateLinks(root, markdown);
        }
        validateRtm(root);

        System.out.printf("RTM validation: %d requirement IDs, %d local links checked.%n",
                checkedRequirements, checkedLinks);
        if (!externalPending.isEmpty()) {
            System.out.println("External evidence pending (not treated as missing files):");
            externalPending.forEach(item -> System.out.println("- " + item));
        }
        if (errors > 0) {
            System.err.printf("RTM validation failed with %d error(s).%n", errors);
            System.exit(1);
        }
        System.out.println("RTM structural validation passed.");
    }

    private static List<Path> findMarkdownFiles(Path root) throws IOException {
        List<Path> files = new ArrayList<>();
        Path readme = root.resolve("README.md");
        if (Files.isRegularFile(readme)) {
            files.add(readme);
        }
        Path docs = root.resolve("docs");
        if (Files.isDirectory(docs)) {
            try (Stream<Path> paths = Files.walk(docs)) {
                paths.filter(path -> Files.isRegularFile(path)
                                && path.getFileName().toString().toLowerCase().endsWith(".md"))
                        .forEach(files::add);
            }
        }
        return files;
    }

    private static void validateLinks(Path root, Path markdown) throws IOException {
        String content = Files.readString(markdown, StandardCharsets.UTF_8);
        Matcher matcher = LINK.matcher(content);
        while (matcher.find()) {
            String target = matcher.group(1).trim();
            if (isExternal(target) || target.startsWith("#")) {
                continue;
            }
            String pathPart = target.split("[?#]", 2)[0];
            if (pathPart.isBlank()) {
                continue;
            }
            Path resolved = markdown.getParent()
                    .resolve(URLDecoder.decode(pathPart, StandardCharsets.UTF_8))
                    .normalize();
            checkedLinks++;
            if (!resolved.startsWith(root) || !Files.exists(resolved)) {
                error(markdown, "broken local link: " + target);
            }
        }
    }

    private static boolean isExternal(String target) {
        String lower = target.toLowerCase();
        return lower.startsWith("http://") || lower.startsWith("https://")
                || lower.startsWith("mailto:") || lower.startsWith("//");
    }

    private static void validateRtm(Path root) throws IOException {
        Path rtm = root.resolve("docs/rtm.md");
        if (!Files.isRegularFile(rtm)) {
            error(rtm, "RTM file is missing");
            return;
        }

        List<String> lines = Files.readAllLines(rtm, StandardCharsets.UTF_8);
        List<String> header = lines.stream()
                .filter(line -> line.startsWith("| ID |"))
                .findFirst()
                .map(ValidateRtm::cells)
                .orElse(List.of());
        int idColumn = header.indexOf("ID");
        int sourceColumn = header.indexOf("Fonte oficial");
        int criticalColumn = header.indexOf("Crítico");
        int requirementColumn = header.indexOf("Requisito");
        int riskColumn = header.indexOf("Risco");
        int implementationColumn = header.indexOf("Implementação");
        int testsColumn = header.indexOf("Testes");
        int evidenceColumn = header.indexOf("Evidência local");
        int externalColumn = header.indexOf("Pendência externa");
        int executionColumn = header.indexOf("Execução local");
        int statusColumn = header.indexOf("Status");
        if (idColumn < 0 || sourceColumn < 0 || criticalColumn < 0
                || requirementColumn < 0 || riskColumn < 0 || implementationColumn < 0
                || testsColumn < 0 || evidenceColumn < 0 || externalColumn < 0
                || executionColumn < 0 || statusColumn < 0) {
            error(rtm, "required RTM columns are missing or have invalid UTF-8 headers");
            return;
        }

        Set<String> ids = new HashSet<>();
        for (String line : lines) {
            if (!line.startsWith("|")) {
                continue;
            }
            List<String> row = cells(line);
            if (row.size() <= statusColumn || !REQUIREMENT_ID.matcher(row.get(idColumn)).matches()) {
                continue;
            }
            String id = row.get(idColumn);
            checkedRequirements++;
            if (!ids.add(id)) {
                error(rtm, "duplicate requirement ID: " + id);
            }

            requireText(rtm, id, "official source", row.get(sourceColumn));
            requireText(rtm, id, "requirement description", row.get(requirementColumn));
            requireText(rtm, id, "risk", row.get(riskColumn));
            requireText(rtm, id, "implementation", row.get(implementationColumn));

            List<String> tests = localLinks(row.get(testsColumn));
            List<String> evidence = localLinks(row.get(evidenceColumn));
            boolean critical = row.get(criticalColumn).equalsIgnoreCase("sim");
            String status = row.get(statusColumn).trim().toUpperCase();
            String execution = row.get(executionColumn).toLowerCase();
            boolean pending = execution.contains("pendente")
                    || execution.contains("não executado")
                    || execution.contains("nao executado")
                    || execution.contains("bloqueado")
                    || execution.contains("não comprovado")
                    || execution.contains("nao comprovado");

            if (critical && tests.isEmpty()) {
                error(rtm, id + " is critical but has no local test reference");
            }
            if (status.startsWith("PASS")) {
                if (tests.isEmpty() || evidence.isEmpty()) {
                    error(rtm, id + " is PASS without local test and evidence links");
                }
                if (pending) {
                    error(rtm, id + " is PASS while local execution is pending or unproven");
                }
                for (String test : tests) {
                    if (!test.toLowerCase().endsWith(".java")) {
                        error(rtm, id + " is PASS but its test link is not a Java test source: " + test);
                    }
                    validateTestReport(root, rtm, id, test);
                }
            } else if (status.startsWith("PARCIAL") && tests.isEmpty() && evidence.isEmpty()
                    && !row.get(externalColumn).toUpperCase().contains("PENDENTE EXTERNO")) {
                error(rtm, id + " is PARTIAL without local evidence or an explicit external pending item");
            } else if (!status.startsWith("PARCIAL") && !status.startsWith("PENDENTE")
                    && !status.startsWith("BLOQUEADO")) {
                error(rtm, id + " has an unsupported status: " + row.get(statusColumn));
            }

            for (String test : tests) {
                validateTestSource(root, rtm, id, test);
            }
            Matcher external = EXTERNAL_PENDING.matcher(row.get(externalColumn));
            while (external.find()) {
                externalPending.add(id + ": " + external.group(1).trim());
            }
        }
        if (checkedRequirements == 0) {
            error(rtm, "no requirement rows were found");
        }
    }

    private static void requireText(Path rtm, String id, String field, String value) {
        if (value == null || value.isBlank() || value.equals("—") || value.equals("-")) {
            error(rtm, id + " has no " + field);
        }
    }

    private static void validateTestSource(Path root, Path rtm, String id, String testLink) {
        if (!testLink.toLowerCase().endsWith(".java")) {
            return;
        }
        Path testSource = rtm.getParent()
                .resolve(URLDecoder.decode(testLink, StandardCharsets.UTF_8))
                .normalize();
        Path testRoot = root.resolve("src/test/java").normalize();
        if (!testSource.startsWith(testRoot) || !Files.isRegularFile(testSource)
                || !testSource.toString().endsWith(".java")) {
            error(rtm, id + " references a missing test source: " + testLink);
        }
    }

    private static void validateTestReport(Path root, Path rtm, String id, String testLink) {
        Path testSource = rtm.getParent()
                .resolve(URLDecoder.decode(testLink, StandardCharsets.UTF_8))
                .normalize();
        Path testRoot = root.resolve("src/test/java").normalize();
        if (!testSource.startsWith(testRoot) || !Files.isRegularFile(testSource)
                || !testSource.toString().endsWith(".java")) {
            return;
        }
        String suiteName = testRoot.relativize(testSource).toString()
                .replace(testSource.getFileSystem().getSeparator(), ".");
        suiteName = suiteName.substring(0, suiteName.length() - ".java".length());
        Path report = root.resolve("target/surefire-reports/TEST-" + suiteName + ".xml");
        if (!Files.isRegularFile(report)) {
            error(rtm, id + " has no current Surefire report for " + suiteName);
            return;
        }
        try {
            String xml = Files.readString(report, StandardCharsets.UTF_8);
            int tests = attribute(xml, "tests");
            int failures = attribute(xml, "failures");
            int suiteErrors = attribute(xml, "errors");
            int skipped = attribute(xml, "skipped");
            if (tests == 0 || failures > 0 || suiteErrors > 0 || skipped > 0) {
                error(rtm, id + " is PASS but " + suiteName
                        + " has tests=" + tests + ", failures=" + failures
                        + ", errors=" + suiteErrors + ", skipped=" + skipped);
            }
        } catch (IOException | IllegalArgumentException exception) {
            error(rtm, "cannot read Surefire report for " + suiteName + ": " + exception.getMessage());
        }
    }

    private static int attribute(String xml, String name) {
        Matcher matcher = Pattern.compile(String.format(ATTRIBUTE.pattern(), Pattern.quote(name))).matcher(xml);
        if (!matcher.find()) {
            throw new IllegalArgumentException("missing XML attribute " + name);
        }
        return Integer.parseInt(matcher.group(1));
    }

    private static List<String> cells(String row) {
        String[] parts = row.split("\\|", -1);
        List<String> cells = new ArrayList<>();
        for (int i = 1; i < parts.length - 1; i++) {
            cells.add(parts[i].trim());
        }
        return cells;
    }

    private static List<String> localLinks(String cell) {
        List<String> paths = new ArrayList<>();
        Matcher matcher = LINK.matcher(cell);
        while (matcher.find()) {
            String target = matcher.group(1).trim();
            if (!isExternal(target) && !target.startsWith("#")) {
                paths.add(target.split("[?#]", 2)[0]);
            }
        }
        return paths;
    }

    private static void error(Path file, String message) {
        errors++;
        System.err.println(file + ": " + message);
    }
}
