# What?
This is console utility for automatical tracking work time from you Bitrix24 calendar to Jira issue.
<br>User confedential data is stored in file `userData.json`, the one provided is just a sample.

# Prerequisites
0. Install groovy
    ```
    sdk install groovy
    ```
1. Create your jira token (piraToken):
    ```
    https://<your_jira_host>/secure/ViewProfile.jspa?selectedTab=com.atlassian.pats.pats-plugin:jira-user-personal-access-tokens
    ```
2. Reveal you jira user key (piraUserId), copy field `key`
    ```
    https://<your_jira_host>/rest/api/2/myself
    ```     
3. Create you bitrix in-hook with api method secret:
    `https://<your_bitrix_host>/devops/section/standard/` -> Входящий вебхук -> Вебхук для вызова rest api 

    There's URL in the field:
    ```
    https://<your_bitrix_host>/rest/<bitrixUserId>/<bitrixApiSecret>/
    ```
    Save bitrixUserId and bitrixApiSecret.
    Also add access rights (Настройка прав ) to Calendar only.

4. Fill all this user data to `userData.json`.


# Usage

To sync your day from bitrix to jira use:
```
groovy sync.groovy 2023-09-09
```

To fill your day work time to max (8 hours) with communication use:
```
groovy fill_communication.groovy 2023-09-09
```
