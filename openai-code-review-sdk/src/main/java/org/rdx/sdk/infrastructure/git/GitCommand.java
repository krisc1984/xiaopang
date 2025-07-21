package org.rdx.sdk.infrastructure.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.rdx.sdk.types.util.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @ author rdx
 * @ describe :
 * @ date  2024/12/3
 **/
public class GitCommand {

private Logger logger = LoggerFactory.getLogger(GitCommand.class);


    private final String githubReviewLogUri;

    private final String githubToken;

    private final String project;

    private final String branch;

    private final String author;

    private final String message;

    public GitCommand(String githubReviewLogUri, String githubToken, String project, String branch, String author, String message) {
        this.githubReviewLogUri = githubReviewLogUri;
        this.githubToken = githubToken;
        this.project = project;
        this.branch = branch;
        this.author = author;
        this.message = message;
    }

    public String getCodeDiff() throws Exception {
        //获取最新提交的hash值
        String hash = getLatestCommitHash();
        System.out.println(hash);
        //通过最新提交的hash值获取代码差异
        String diffCode = getCommitDiff(hash);
        System.out.println(diffCode);

        return diffCode;
    }

    private String getCommitDiff(String hash)throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", hash + "^", hash);
        processBuilder.directory(new File("."));
        Process process = processBuilder.start();

        StringBuilder builder = new StringBuilder();
        try(BufferedReader diffReader =new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader =new BufferedReader(new InputStreamReader(process.getInputStream()))
        ){
            String line;
            while((line= diffReader.readLine())!= null){
                builder.append(line).append(System.lineSeparator());
            }
            String errorLine;
            while((errorLine = errorReader.readLine())!=null){
                logger.info("error:"+errorLine);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0){
                logger.info("获取代码diff成功");
            }
            return builder.toString();
        }

    }

    private String getLatestCommitHash()throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
        processBuilder.directory(new File("."));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

            String latestHash = reader.readLine();
            if (latestHash == null) {
                logger.info("未获取到提交信息");
            }
            int exitCode = process.waitFor();
            if (exitCode == 0){
                logger.info("获取hash值成功");
            }
            return latestHash;
        }

    }
    public String commitAndPush(String recommend) throws Exception {
        Git git = Git.cloneRepository()
                .setURI(githubReviewLogUri )
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, ""))
                .call();

        // 创建分支
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
        }

        String fileName = project + "-" + branch + "-" + author + System.currentTimeMillis() + "-" + RandomStringUtils.randomNumeric(4) + ".md";
        File newFile = new File(dateFolder, fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(recommend);
        }

        // 提交内容
        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("add code review new file" + fileName).call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(githubToken, "")).call();

        logger.info("openai-code-review git commit and push done! {}", fileName);

        return githubReviewLogUri + "/blob/master/" + dateFolderName + "/" + fileName;
    }

    public String getProject() {
        return project;
    }

    public String getBranch() {
        return branch;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }

}
