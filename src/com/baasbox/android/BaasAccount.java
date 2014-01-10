package com.baasbox.android;

import com.baasbox.android.exceptions.BAASBoxException;
import com.baasbox.android.json.JsonException;
import com.baasbox.android.json.JsonObject;
import com.baasbox.android.spi.CredentialStore;
import com.baasbox.android.spi.Credentials;
import com.baasbox.android.spi.HttpRequest;

import org.apache.http.HttpResponse;

/**
 * This class represents the account of the user on BaasBox.
 * Created by Andrea Tortorella on 02/01/14.
 */
public class BaasAccount extends BaasPerson {

    public final String password;

    public BaasAccount(String username, String password) {
        super(username);
        this.password = password;
    }

    private BaasAccount(String username, String password, JsonObject data) {
        super(username, data);
        this.password = password;
    }

    public BaasDisposer signup(BAASBox client, BAASBox.BAASHandler<Void, ?> handler) {
        return signup(client, null, 0, handler);
    }

    public BaasDisposer signup(BAASBox client, int priority, BAASBox.BAASHandler<Void, ?> handler) {
        return signup(client, null, priority, handler);
    }

    public <T> BaasDisposer signup(BAASBox client, T tag, BAASBox.BAASHandler<Void, T> handler) {
        return signup(client, tag, 0, handler);
    }

    /**
     * Returns wether the user is currently logged in
     *
     * @param client
     * @return true if the user is logged in
     */
    public static boolean isUserLoggedIn(BAASBox client) {
        Credentials credentials = client.credentialStore.get(true);
        return credentials != null && credentials.username != null && credentials.password != null;
    }

    /**
     * Asynchronously signups a user to BAASBox
     *
     * @param client   an instance of the service
     * @param tag      a custom tag usable as user data
     * @param priority an integer priority for the request
     * @param handler  an handler t be called upon request completion
     * @param <T>      the type of the custom tag
     * @return a {@link com.baasbox.android.BaasDisposer} to control the execution of this request
     */
    public <T> BaasDisposer signup(BAASBox client, T tag, int priority, BAASBox.BAASHandler<Void, T> handler) {
        RequestFactory factory = client.requestFactory;
        String endpoint = factory.getEndpoint("user");
        HttpRequest request = factory.post(endpoint, toJson());
        BaasRequest<Void, T> breq = new BaasRequest<Void, T>(request, priority, tag, new SignupParser(username, password), handler, false);
        return client.submitRequest(breq);
    }

    public BaasDisposer login(BAASBox client, BAASBox.BAASHandler<Void, ?> handler) {
        return login(client, null, 0, handler);
    }

    public <T> BaasDisposer login(BAASBox client, T tag, BAASBox.BAASHandler<Void, T> handler) {
        return login(client, tag, 0, handler);
    }

    public BaasDisposer login(BAASBox client, int priority, BAASBox.BAASHandler<Void, ?> handler) {
        return login(client, null, priority, handler);
    }

    public <T> BaasDisposer login(BAASBox client, T tag, int priority, BAASBox.BAASHandler<Void, T> handler) {
        return client.submitRequest(new LoginRequest<T>(client, username, password, priority, tag, handler));
    }

    public static BaasDisposer get(BAASBox client, BAASBox.BAASHandler<BaasAccount, ?> handler) {
        return get(client, null, 0, handler);
    }

    public static BaasDisposer get(BAASBox client, int priority, BAASBox.BAASHandler<BaasAccount, ?> handler) {
        return get(client, null, priority, handler);
    }

    public static <T> BaasDisposer get(BAASBox client, T tag, BAASBox.BAASHandler<BaasAccount, T> handler) {
        return get(client, tag, 0, handler);
    }

    public static <T> BaasDisposer get(BAASBox client, T tag, int priority, BAASBox.BAASHandler<BaasAccount, T> handler) {
        RequestFactory factory = client.requestFactory;
        String endpoint = factory.getEndpoint("me");
        HttpRequest get = factory.get(endpoint);
        BaasRequest<BaasAccount, T> breq = new BaasRequest<BaasAccount, T>(get, priority, tag, profileParser, handler, true);
        return client.submitRequest(breq);
    }

    public BaasDisposer save(BAASBox client, BAASBox.BAASHandler<BaasAccount, ?> handler) {
        return save(client, null, 0, handler);
    }

    public BaasDisposer save(BAASBox client, int priority, BAASBox.BAASHandler<BaasAccount, ?> handler) {
        return save(client, null, priority, handler);
    }

    public <T> BaasDisposer save(BAASBox client, T tag, BAASBox.BAASHandler<BaasAccount, T> handler) {
        return save(client, tag, 0, handler);
    }

    //todo handle merge profile
    public <T> BaasDisposer save(BAASBox client, T tag, int priority, BAASBox.BAASHandler<BaasAccount, T> handler) {
        RequestFactory factory = client.requestFactory;
        String endpoint = factory.getEndpoint("me");
        HttpRequest request = factory.put(endpoint, toJson(false));
        BaasRequest<BaasAccount, T> breq = new BaasRequest<BaasAccount, T>(request, priority, tag, profileParser, handler, true);
        return client.submitRequest(breq);
    }


    public static BaasDisposer logout(BAASBox client, BAASBox.BAASHandler<Void, ?> handler) {
        return logout(client, null, 0, handler);
    }


    public static BaasDisposer logout(BAASBox client, int priority, BAASBox.BAASHandler<Void, ?> handler) {
        return logout(client, null, priority, handler);
    }

    public static <T> BaasDisposer logout(BAASBox client, T tag, BAASBox.BAASHandler<Void, T> handler) {
        return logout(client, tag, 0, handler);
    }

    public static <T> BaasDisposer logout(BAASBox client, T tag, int priority, BAASBox.BAASHandler<Void, T> handler) {
        RequestFactory factory = client.requestFactory;
        String endpoint = factory.getEndpoint("logout");
        HttpRequest post = factory.post(endpoint, null, null);
        BaasRequest<Void, T> breq = new BaasRequest<Void, T>(post, priority, tag, logoutParser, handler, false);
        return client.submitRequest(breq);
    }

    @Override
    protected JsonObject toJson(boolean credentials) {
        JsonObject object = super.toJson(credentials);
        if (credentials) object.putString("password", password);
        return object;
    }

    private static final ResponseParser<BaasAccount> accountProfile = new BaseResponseParser<BaasAccount>() {
        @Override
        protected BaasAccount handleOk(BaasRequest<BaasAccount, ?> request, HttpResponse response, BAASBox.Config config, CredentialStore credentialStore) throws BAASBoxException {
            try {
                JsonObject content = getJsonEntity(response, config.HTTP_CHARSET);
                JsonObject object = content.getObject("data");
                Credentials credentials = credentialStore.get(true);
                BaasAccount account = new BaasAccount(credentials.username, credentials.password, object);
                return account;
            } catch (JsonException e) {
                throw new BAASBoxException("Unable to parse server response", e);
            }
        }
    };

    private static final ResponseParser<BaasAccount> profileParser = new BaseResponseParser<BaasAccount>() {
        @Override
        protected BaasAccount handleOk(BaasRequest<BaasAccount, ?> request, HttpResponse response, BAASBox.Config config, CredentialStore credentialStore) throws BAASBoxException {
            try {
                JsonObject content = getJsonEntity(response, config.HTTP_CHARSET);
                JsonObject object = content.getObject("data");
                Credentials credentials = credentialStore.get(true);
                BaasAccount account = new BaasAccount(credentials.username, credentials.password, object);
                return account;
            } catch (JsonException e) {
                throw new BAASBoxException("Unable to parse server response", e);
            }
        }
    };

    private final static class SignupParser extends BaseResponseParser<Void> {
        private final String username;
        private final String password;

        SignupParser(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Void handleOk(BaasRequest<Void, ?> request, HttpResponse response, BAASBox.Config config, CredentialStore credentialStore) throws BAASBoxException {
            try {
                JsonObject content = getJsonEntity(response, config.HTTP_CHARSET);
                JsonObject data = content.getObject("data");
                String token = data.getString("X-BB-SESSION");
                Credentials c = new Credentials();
                c.username = username;
                c.password = password;
                c.sessionToken = token;
                credentialStore.set(c);
//                credentialStore.updateToken(token);
                return null;
            } catch (JsonException e) {
                throw new BAASBoxException("Could not parse server response", e);
            }
        }
    }

    private static final ResponseParser<Void> logoutParser = new BaseResponseParser<Void>() {
        @Override
        protected Void handleOk(BaasRequest<Void, ?> request, HttpResponse response, BAASBox.Config config, CredentialStore credentialStore) throws BAASBoxException {
            try {
                credentialStore.set(null);
                return null;
            } catch (Exception e) {
                throw new BAASBoxException("Error logging out", e);
            }
        }
    };
}
