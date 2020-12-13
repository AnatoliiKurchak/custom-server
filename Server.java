import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    private static ExecutorService executorService = Executors.newFixedThreadPool(4);
    private static ParameterParser parser = new ParameterParser();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(8080);

        while (true) {
            Socket socket = serverSocket.accept();
            Task task = new Task(socket, parser);
            executorService.submit(task);
        }
    }
}

class Task implements Callable<Void> {

    private static final String OUTPUT = "<html><head><title>Example</title></head><body><p>You've send us request with parameters %s</p></body></html>";
    private static final String OUTPUT_HEADERS = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html\r\n" +
            "Content-Length: ";
    private static final String OUTPUT_END_OF_HEADERS = "\r\n\r\n";

    private Socket socket;

    private ParameterParser parser;

    public Task(Socket socket, ParameterParser parser) {
        this.socket = socket;
        this.parser = parser;
    }

    @Override
    public Void call() throws Exception {
        try (
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))
        ) {
            String pathWithParameters = reader.readLine();
            List<String> parameters = parser.parseParameters(pathWithParameters);
            String joinedParameters = String.join(" and ", parameters);
            String output = String.format(OUTPUT, joinedParameters);
	    System.out.println(output);
            writer.write(OUTPUT_HEADERS + output.length() + OUTPUT_END_OF_HEADERS + output);
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
