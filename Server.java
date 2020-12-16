import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Server {

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static ParameterParser parser = new ParameterParser();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);

        String fileContentPath = System.getenv("com.teoretyk0706.path");

        System.out.println(fileContentPath);

        while (true) {
            Socket socket = serverSocket.accept();
            Task task = new Task(socket, parser, fileContentPath);
            executorService.submit(task);
        }
    }
}

class Task implements Callable<Void> {
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";
    private static final String OUTPUT = "<html><head><title>Example</title></head><body><p>Request sent on %s. You've send us request with parameters %s</p></body></html>";

    private Socket socket;

    private ParameterParser parser;

    private String fileContentPath;

    public Task(Socket socket, ParameterParser parser, String fileContentPath) {
        this.socket = socket;
        this.parser = parser;
        this.fileContentPath = fileContentPath;
    }

    @Override
    public Void call() throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

            String pathWithParameters = reader.readLine();

            List<String> parameters = parser.parseParameters(pathWithParameters);

            String joinedParameters = String.join(" and ", parameters);

            String output = String.format(OUTPUT, new Date(), joinedParameters);

            String fileContent = Files.lines(Paths.get(fileContentPath, "test.txt"))
                    .map(line -> "<br>" + line + "<br/>")
                    .collect(Collectors.joining("\r\n"));

            String response = output.concat("<br/>").concat(fileContent);

            System.out.println(response);

            writer.write(OUTPUT_HEADERS + response.length() + OUTPUT_END_OF_HEADERS + response);
            writer.flush();
        }

        return null;
    }
}


class ParameterParser {

    private static final String QUESTION_MARK = "?";
    private static final String HTTP = "HTTP";
    private static final String AMPERSAND = "&";

    public List<String> parseParameters(String pathWithParameters) {
        if (!pathWithParameters.contains(QUESTION_MARK)) {
            return Collections.singletonList("no parameters");
        }

        int firstIndexOfParameters = pathWithParameters.indexOf(QUESTION_MARK) + 1;

        int lastIndexOfParameters = pathWithParameters.indexOf(HTTP);

        String parameters = pathWithParameters.substring(firstIndexOfParameters, lastIndexOfParameters).trim();

        return Arrays.asList(parameters.split(AMPERSAND));
    }
}
