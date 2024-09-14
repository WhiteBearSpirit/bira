import groovy.json.JsonSlurper
import groovy.json.JsonOutput

class Bira {

    static final int TOTAL_DAYTIME_SECONDS = 28800;
    static final int MAX_COMMUNICATION_TIME = 3600;

    static int fetchWorkTimeFromPira(String dateString, String worker, String piraToken) {

        print("Fetching total work time from pira for ${dateString}... ");
        String urlString = "https://<your_jira_host>/rest/tempo-timesheets/4/worklogs/search";
        String bodyString = String.format(
            '{"from":"%s","to":"%s","worker":["%s"]}',
            dateString, dateString, worker
        );
        HttpURLConnection post = new URL(urlString).openConnection();
        post.setRequestMethod("POST");
        post.setRequestProperty ("Authorization", "Bearer " + piraToken);
        post.setDoOutput(true);
        post.setRequestProperty("Content-Type", "application/json");
        post.getOutputStream().write(bodyString.getBytes("UTF-8"));
        int postRC = post.getResponseCode();
        print("Code ${postRC}. ");
        if (postRC != 200) { 
            throw new Exception("Response code was ${postRC}");
        }
        String responseString = post.getInputStream().getText();
        Object j = new groovy.json.JsonSlurper().parseText(responseString);
        int sum = j.timeSpentSeconds.sum();
        println("Got ${sum} seconds.");
        return sum;
    }

    static void sendCommunicationToPira(String dateString, int duration, String worker, String piraToken) {

        print("Sending communication event with duration ${duration} to pira... ");
        String urlString = "https://<your_jira_host>/rest/tempo-timesheets/4/worklogs/";
        String bodyString = String.format(
            '{"started":"%s","timeSpentSeconds":%s,"originTaskId":"HRL-4244", "worker":"%s", "comment":"Autofilled with groovy script", "attributes":{"_Статья_":{"name":"Статья","workAttributeId":8,"value":"WORK"}}}',
            dateString, duration, worker
        );
        HttpURLConnection post = new URL(urlString).openConnection();
        post.setRequestMethod("POST");
        post.setRequestProperty ("Authorization", "Bearer " + piraToken);
        post.setDoOutput(true);
        post.setRequestProperty("Content-Type", "application/json");
        post.getOutputStream().write(bodyString.getBytes("UTF-8"));
        int postRC = post.getResponseCode();
        println(postRC);
    }

    static void main(String[] args) {

        String dateString = args[0];
        Date.parse('yyyy-MM-dd', dateString) // validation
        File userDataFile = new File("userData.json");
        Object userData = new groovy.json.JsonSlurper().parseText(userDataFile.getText());
        //println(userData)
        int spendTime = fetchWorkTimeFromPira(dateString, userData.piraUserId, userData.piraToken);
        int fillTime = Math.min(TOTAL_DAYTIME_SECONDS - spendTime, MAX_COMMUNICATION_TIME);
        if (fillTime > 0) {
            sendCommunicationToPira(dateString, fillTime, userData.piraUserId, userData.piraToken);
        }
    }   
}