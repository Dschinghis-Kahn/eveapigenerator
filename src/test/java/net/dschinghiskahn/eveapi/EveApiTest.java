package net.dschinghiskahn.eveapi;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.dschinghiskahn.eveapi.api.calllist.Call;
import net.dschinghiskahn.eveapi.api.calllist.CallList;
import net.dschinghiskahn.eveapi.character.assetlist.Asset;
import net.dschinghiskahn.eveapi.character.assetlist.AssetList;
import net.dschinghiskahn.eveapi.character.mailmessages.MailMessages;
import net.dschinghiskahn.eveapi.character.mailmessages.Message;
import net.dschinghiskahn.eveapi.character.notifications.Notification;
import net.dschinghiskahn.eveapi.character.notifications.Notifications;
import net.dschinghiskahn.eveapi.character.upcomingcalendarevents.UpcomingCalendarEvents;
import net.dschinghiskahn.eveapi.character.upcomingcalendarevents.UpcomingEvent;

public class EveApiTest {

    private static long keyId;
    private static String verificationCode;
    private static long characterId;

    @BeforeClass
    public static void init() {
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getRootLogger().removeAllAppenders();
        ConsoleAppender appender = new ConsoleAppender();
        appender.setLayout(new PatternLayout("%d %-5p: %m%n"));
        appender.activateOptions();
        Logger.getRootLogger().addAppender(appender);

        Properties properties = new Properties();
        try {
            properties.load(EveApiTest.class.getClassLoader().getResourceAsStream("test.properties"));
            String value = properties.getProperty("keyId", "");
            if (value != null && value.length() > 0) {
                try {
                    keyId = Long.parseLong(properties.getProperty("keyId", ""));
                } catch (NumberFormatException e) {
                    Assert.fail("KeyId is not a valid number!");
                }
            } else {
                Assert.fail("KeyId is not configured in test.properties!");
            }
            value = properties.getProperty("verificationCode", "");
            if (value != null && value.length() > 0) {
                verificationCode = properties.getProperty("verificationCode", "");
            } else {
                Assert.fail("VerificationCode is not configured in test.properties!");
            }
            value = properties.getProperty("characterId", "");
            if (value != null && value.length() > 0) {
                try {
                    characterId = Long.parseLong(properties.getProperty("characterId", ""));
                } catch (NumberFormatException e) {
                    Assert.fail("CharacterId is not a valid number!");
                }
            } else {
                Assert.fail("CharacterId is not configured in test.properties!");
            }
        } catch (IOException e) {
            Logger.getLogger(EveApiTest.class).error("File \"test.properties\" not found!", e);
            Assert.fail("File test.properties not found!");
        }
    }

    @Test
    public void hasAllEndpoints() throws IOException, EveApiException {
        Logger.getLogger(getClass()).info("Running test: hasAllEndpoints()");
        CallList callList = Api.getCallList(keyId, verificationCode);
        StringBuilder missing = new StringBuilder();
        for (Call call : callList.getCalls()) {
            if (!"Character".equals(call.getType())) {
                continue;
            }
            boolean found = false;
            for (Method method : Api.class.getMethods()) {
                if (method.getName().equals("get" + call.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Logger.getLogger(getClass()).error("Endpoint missing: " + call.getName());
                missing.append(call.getName());
                missing.append(',');
            }
        }
        if (missing.length() > 0) {
            missing.deleteCharAt(missing.length() - 1);
            Assert.fail("Endpoints missing: " + missing.toString());
        }
    }

    @Test
    public void testAllEndpoints() throws IOException, EveApiException {
        Logger.getLogger(getClass()).info("Running test: testAllEndpoints()");
        CallList callList = Api.getCallList(keyId, verificationCode);
        StringBuilder missing = new StringBuilder();
        for (Call call : callList.getCalls()) {
            if (!"Character".equals(call.getType())) {
                continue;
            }
            boolean found = false;
            for (Method method : getClass().getMethods()) {
                if (method.getName().equals("get" + call.getName())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                Logger.getLogger(getClass()).error("Endpoint missing: " + call.getName());
                missing.append(call.getName());
                missing.append(',');
            }
        }
        if (missing.length() > 0) {
            missing.deleteCharAt(missing.length() - 1);
            Assert.fail("Endpoints missing: " + missing.toString());
        }
    }

    @Test()
    public void getErrorResponse203() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getErrorResponse203()");
        try {
            Assert.assertNotNull(Api.getAccountBalance(1L, "", 0L));
        } catch (EveApiException e) {
            Assert.assertEquals("Wrong error code!", e.getCode().longValue(), 203L);
            Assert.assertEquals("Wrong error message!", e.getMessage(), "Authentication failure.");
            return;
        }
        Assert.fail("Exception expected!");
    }

    @Test()
    public void getErrorResponse221() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getErrorResponse203()");
        try {
            Assert.assertNotNull(Api.getAccountBalance(0L, "", 0L));
        } catch (EveApiException e) {
            Assert.assertEquals("Wrong error code!", e.getCode().longValue(), 221);
            Assert.assertEquals("Wrong error message!", e.getMessage(), "Illegal page request! Please verify the access granted by the key you are using!");
            return;
        }
        Assert.fail("Exception expected!");
    }

    @Test
    public void getAccountBalance() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getAccountBalance()");
        try {
            Assert.assertNotNull(Api.getAccountBalance(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getAccountStatus() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getAccountStatus()");
        try {
            Assert.assertNotNull(Api.getAccountStatus(keyId, verificationCode));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getAssetList() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getAssetList()");
        try {
            Assert.assertNotNull(Api.getAssetList(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getBookmarks() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getBookmarks()");
        try {
            Assert.assertNotNull(Api.getBookmarks(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getCalendarEventAttendees() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getCalendarEventAttendees()");
        try {
            UpcomingCalendarEvents events = Api.getUpcomingCalendarEvents(keyId, verificationCode, characterId);
            if (events == null || events.getUpcomingEvents().size() == 0) {
                Logger.getLogger(getClass()).warn("Could not test API: Depending request returned nothing.");
            } else {
                for (UpcomingEvent event : events.getUpcomingEvents()) {
                    try {
                        Assert.assertNotNull(Api.getCalendarEventAttendees(keyId, verificationCode, characterId, event.getEventId()));
                    } catch (EveApiException e) {
                        Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
                    }
                }
            }
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }

    }

    @Test
    public void getCharacterInfo() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getCharacterInfo()");
        try {
            Assert.assertNotNull(Api.getCharacterInfo(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getCharacters() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getCharacters()");
        try {
            Assert.assertNotNull(Api.getCharacters(keyId, verificationCode));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getCharacterSheet() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getCharacterSheet()");
        try {
            Assert.assertNotNull(Api.getCharacterSheet(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getChatChannels() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getChatChannels()");
        try {
            Assert.assertNotNull(Api.getChatChannels(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getContactList() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getContactList()");
        try {
            Assert.assertNotNull(Api.getContactList(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getContactNotifications() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getContactNotifications()");
        try {
            Assert.assertNotNull(Api.getContactNotifications(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getContracts() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getContracts()");
        try {
            Assert.assertNotNull(Api.getContracts(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getFacWarStats() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getFacWarStats()");
        try {
            Assert.assertNotNull(Api.getFacWarStats(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getIndustryJobs() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getIndustryJobs()");
        try {
            Assert.assertNotNull(Api.getIndustryJobs(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getKillLog() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getKillLog()");
        try {
            Assert.assertNotNull(Api.getKillLog(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getLocations() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getLocations()");
        try {
            AssetList assetList = Api.getAssetList(keyId, verificationCode, characterId);
            if (assetList == null || assetList.getAssets().size() == 0) {
                Logger.getLogger(getClass()).warn("Could not test API: Depending request returned nothing.");
            } else {
                for (Asset asset : assetList.getAssets()) {
                    try {
                        Assert.assertNotNull(Api.getLocations(keyId, verificationCode, characterId, asset.getItemId()));
                    } catch (EveApiException e) {
                        Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
                    }
                }
            }
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getMailBodies() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getMailBodies()");
        try {
            MailMessages messages = Api.getMailMessages(keyId, verificationCode, characterId);
            if (messages == null || messages.getMessages().size() == 0) {
                Logger.getLogger(getClass()).warn("Could not test API: Depending request returned nothing.");
            } else {
                for (Message message : messages.getMessages()) {
                    try {
                        Assert.assertNotNull(Api.getMailBodies(keyId, verificationCode, characterId, message.getMessageId()));
                    } catch (EveApiException e) {
                        Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
                    }
                }
            }
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getMailingLists() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getMailingLists()");
        try {
            Assert.assertNotNull(Api.getMailingLists(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getMailMessages() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getMailMessages()");
        try {
            Assert.assertNotNull(Api.getMailMessages(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getMarketOrders() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getMarketOrders()");
        try {
            Assert.assertNotNull(Api.getMarketOrders(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getMedals() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getMedals()");
        try {
            Assert.assertNotNull(Api.getMedals(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getNotifications() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getNotifications()");
        try {
            Assert.assertNotNull(Api.getNotifications(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getNotificationTexts() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getNotificationTexts()");
        try {
            Notifications notifications = Api.getNotifications(keyId, verificationCode, characterId);
            if (notifications == null || notifications.getNotifications().size() == 0) {
                Logger.getLogger(getClass()).warn("Could not test API: Depending request returned nothing.");
            } else {
                for (Notification notification : notifications.getNotifications()) {
                    try {
                        Assert.assertNotNull(Api.getNotificationTexts(keyId, verificationCode, characterId, notification.getNotificationId()));
                    } catch (EveApiException e) {
                        Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
                    }
                }
            }
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }

    }

    @Test
    public void getResearch() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getResearch()");
        try {
            Assert.assertNotNull(Api.getResearch(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getSkillInTraining() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getSkillInTraining()");
        try {
            Assert.assertNotNull(Api.getSkillInTraining(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getSkillQueue() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getSkillQueue()");
        try {
            Assert.assertNotNull(Api.getSkillQueue(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getStandings() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getStandings()");
        try {
            Assert.assertNotNull(Api.getStandings(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getUpcomingCalendarEvents() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getUpcomingCalendarEvents()");
        try {
            Assert.assertNotNull(Api.getUpcomingCalendarEvents(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getWalletJournal() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getWalletJournal()");
        try {
            Assert.assertNotNull(Api.getWalletJournal(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }

    @Test
    public void getWalletTransactions() throws IOException {
        Logger.getLogger(getClass()).info("Running test: getWalletTransactions()");
        try {
            Assert.assertNotNull(Api.getWalletTransactions(keyId, verificationCode, characterId));
        } catch (EveApiException e) {
            Logger.getLogger(getClass()).warn("Could not test API: " + e.getMessage());
        }
    }
}
