import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import com.openai.api.ApiException;
import com.openai.api.models.CompletionRequest;
import com.openai.api.models.CompletionResponse;
import com.openai.api.models.EngineName;
import com.openai.api.models.OpenAIRequest;
import com.openai.api.models.OpenAIResponse;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.util.EncodingUtils;

public class Main {
    private static final String OPENAI_API_KEY = "123";
    private static final String GITHUB_ACCESS_TOKEN = "YOUR_GITHUB_ACCESS_TOKEN";
    private static final String REPOSITORY_NAME = "YOUR_REPOSITORY_NAME";
    private static final String FILENAME_PREFIX = "answer_";

    public static void main(String[] args) throws Exception {
        // Set up the OpenAI API client
        com.openai.api.api.DefaultApi openaiApi = new com.openai.api.api.DefaultApi();
        openaiApi.getApiClient().setApiKey(OPENAI_API_KEY);

        // Set up the GitHub API client
        RepositoryService repositoryService = new RepositoryService();
        repositoryService.getClient().setOAuth2Token(GITHUB_ACCESS_TOKEN);

        // Ask the user three questions
        Scanner scanner = new Scanner(System.in);
        for (int i = 1; i <= 3; i++) {
            System.out.println("Question " + i + ": ");
            String question = scanner.nextLine();

            // Generate a response using the OpenAI API
            CompletionRequest completionRequest = new CompletionRequest();
            completionRequest.setPrompt(question);
            completionRequest.setMaxTokens(100);
            completionRequest.setTemperature(0.5);
            completionRequest.setEngine(EngineName.DA_VINCI);
            OpenAIRequest openaiRequest = new OpenAIRequest().addExamplesItem(completionRequest);
            OpenAIResponse openaiResponse = openaiApi.completions(openaiRequest);

            // Save the response to a file
            String fileName = FILENAME_PREFIX + i + ".txt";
            String responseText = openaiResponse.getChoices().get(0).getText();
            saveToFile(fileName, responseText);

            System.out.println("Saved response to " + fileName);
        }

        // Create a new GitHub repository
        Repository repository = new Repository();
        repository.setName(REPOSITORY_NAME);
        repositoryService.createRepository(repository);

        // Upload the files to the repository
        ContentsService contentsService = new ContentsService(repositoryService.getClient());
        for (int i = 1; i <= 3; i++) {
            String fileName = FILENAME_PREFIX + i + ".txt";
            File file = new File(fileName);
            String fileContent = EncodingUtils.toBase64(file);
            RepositoryContents content = new RepositoryContents();
            content.setPath(fileName);
            content.setContent(fileContent);
            contentsService.createFile(repository, content, "Initial commit");
            System.out.println("Uploaded " + fileName + " to repository.");
        }
    }

    private static void saveToFile(String fileName, String content) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        writer.write(content);
        writer.close();
    }
}
