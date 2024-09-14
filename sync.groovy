import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.EqualsAndHashCode

class Bira {

    @EqualsAndHashCode
    static class BitrixEvent {

        String id;
        String name;
        int dt_in_seconds;

        BitrixEvent(String id, String name, int dt_in_seconds) {
            this.id = id;
            this.name = name;
            this.dt_in_seconds = dt_in_seconds;
        }

        String toString() {
            return String.format("[id=%s, name=%s, dur=%s]", this.id, this.name, this.dt_in_seconds)
        }
    }

    static BitrixEvent[] fetchEvents(int userId, String token, String dateString) {

        print("Fetching calendar events from bitrix for ${dateString}... ");
        String urlString = String.format(
            "https://<your_bitrix_host>/rest/%s/%s/calendar.event.get.json?type=user&ownerId=%s&from=%s&to=%s", 
            userId, token, userId, dateString, dateString
        );
        HttpURLConnection post = new URL(urlString).openConnection();
        post.setRequestMethod("POST");
        post.setRequestProperty("Content-Type", "application/json");
        int postRC = post.getResponseCode();
        print("Code ${postRC}. ");
        if (postRC != 200) { 
            throw new Exception("Response code was ${postRC}");
        }
        String responseString = post.getInputStream().getText();
        Object j = new groovy.json.JsonSlurper().parseText(responseString);
        BitrixEvent[] data = j.result.collect{rec -> parse(rec, Date.parse('yyyy-MM-dd', dateString))};
        BitrixEvent[] filteredData = data.findAll(); // filtering null's
        println("Got ${filteredData.size()} items.");
        return filteredData; 
    }

    static BitrixEvent parse(Object o, Date targetDate) {

        Date eventDate = Date.parse( 'dd.MM.yyyy', o.DATE_FROM);
        if (!targetDate.equals(eventDate)) { return null; }
        return new BitrixEvent(o.ID, o.NAME, (int) o.DT_LENGTH);
    }

    static void sendToPira(String dateString, int duration, String worker, String comment, String piraToken) {

        print("Sending event ${comment} with duration ${duration} to pira... ");
        String urlString = "https://<your_jira_host>/rest/tempo-timesheets/4/worklogs/"
        String bodyString = String.format(
            '{"started":"%s","timeSpentSeconds":%s,"originTaskId":"HRL-4245","worker":"%s","comment":"%s","attributes":{"_Статья_":{"name":"Статья","workAttributeId":8,"value":"WORK"}}}',
            dateString, duration, worker, comment
        )
        //println(bodyString);
        HttpURLConnection post = new URL(urlString).openConnection();
        post.setRequestMethod("POST");
        post.setRequestProperty ("Authorization", "Bearer " + piraToken);
        post.setDoOutput(true);
        post.setRequestProperty("Content-Type", "application/json");
        post.getOutputStream().write(bodyString.getBytes("UTF-8"));
        int postRC = post.getResponseCode();
        println("Code ${postRC}. ");
        //String responseString = post.getInputStream().getText();
        //println(responseString);
    }

    static void main(String[] args) {

        String dateString = args[0];
        Date.parse('yyyy-MM-dd', dateString) // validation
        File userDataFile = new File("userData.json");
        Object userData = new groovy.json.JsonSlurper().parseText(userDataFile.getText());
        BitrixEvent[] data = fetchEvents(userData.bitrixUserId, userData.bitrixApiSecret, dateString);
        //println(data)
        for (rec in data) {
            sendToPira(dateString, rec.dt_in_seconds, userData.piraUserId, rec.name, userData.piraToken);
        }
    }   
}