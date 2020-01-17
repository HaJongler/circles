package space.harbour.jonathan.circles;

import android.content.Context;
import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataManager {

    private Context context;
    private JSONObject data;

    public DataManager(Context context) {
        this.context = context;
        if (isSavedStateFilePresent()) {
            loadDataJson();
            System.out.println("Loaded");
        } else {
            createNewDataJson();
            System.out.println("Created");
        }
    }

    private boolean isSavedStateFilePresent() {
        File file = new File(context.getFilesDir().getAbsolutePath(), context.getString(R.string.saved_state_file));
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        } else if (file.length() == 0) {
            return false;
        }
        return true;
    }

    private void loadDataJson() {
        try {
            File file = new File(context.getFilesDir().getAbsolutePath(), context.getString(R.string.saved_state_file));
            BufferedReader br = new BufferedReader(new FileReader(file));
            String json = br.readLine();
            if (json.length() < 1) {
                createNewDataJson();
                return;
            }
            data = new JSONObject(json);
        } catch (FileNotFoundException fileNotFound) {
            fileNotFound.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void createNewDataJson() {
        // If there's no data, create the default circle called "Friends"
        data = new JSONObject();
        addCircle("Friends");
        addMessageToCircle("Hey!", "Friends");
    }

    public JSONObject getData() {
        return data;
    }

    private JSONObject getCircleData(String circle) {
        try {
            return data.getJSONObject(circle);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getAllCircles() {
        Iterator<String> keys = data.keys();
        return Lists.newArrayList(keys);
    }

    public boolean addCircle(String circle) {
        try {
            List<String> currentCircles = getAllCircles();
            if (currentCircles.contains(circle)) return false;
            data.put(circle, new JSONObject()
                    .put("Messages", new JSONArray())
                    .put("Contacts", new JSONArray()));
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeCircle(String circle) {
        data.remove(circle);
    }

    public List<String> getCircleMessages(String circle) {
        JSONArray messagesArray = null;
        try {
            messagesArray = getCircleData(circle).getJSONArray("Messages");
            ArrayList<String> messages = new ArrayList<>();
            for (int i = 0; i < messagesArray.length(); i++) {
                messages.add(messagesArray.get(i).toString());
            }
            return messages;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<Contact> getCircleContacts(String circle) {
        try {
            JSONArray contactsArray = getCircleData(circle).getJSONArray("Contacts");
            List<Contact> contacts = new ArrayList<>();
            for (int i = 0; i < contactsArray.length(); i++) {
                JSONObject contactJson = (JSONObject) contactsArray.get(i);
                contacts.add(new Contact(contactJson));
            }
            return contacts;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean addContactToCircle(Contact contact, String circle) {
        List<Contact> circleContacts = getCircleContacts(circle);
        for (Contact contact1 : circleContacts) {
            if (contact1.getId().equals(contact.getId())) return false;
        }
        circleContacts.add(contact);
        try {
            JSONArray contactsJson = new JSONArray();
            for (Contact contact1 : circleContacts) contactsJson.put(contact1.asJSONObj());
            data.getJSONObject(circle).put("Contacts", contactsJson);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void removeContactFromCircle(Contact contact, String circle) {
        try {
            JSONArray contactsJson = new JSONArray();
            for (Contact contact1 : getCircleContacts(circle)) {
                if (!contact1.getId().equals(contact.getId())) {
                    contactsJson.put(contact1.asJSONObj());
                }
            }
            data.getJSONObject(circle).put("Contacts", contactsJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void repositionCircleContact(Contact contact, String circle) {
        removeContactFromCircle(contact, circle);
        addContactToCircle(contact, circle);
    }

    public boolean addMessageToCircle(String message, String circle) {
        try {
            JSONArray ja = getCircleData(circle).getJSONArray("Messages");
            for (int i = 0; i < ja.length(); i++)
                if (ja.getString(i).equals(message))
                    return false;
            ja.put(message);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String removeMessageFromCircle(Integer index, String circle) {
        try {
            return (String) getCircleData(circle).getJSONArray("Messages").remove(index);
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void saveData() {
        System.out.println(data.toString());
        try {
            FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), context.getString(R.string.saved_state_file)));
            fos.write(data.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
