package at.grahsl.kafka.connect.mongodb;

import com.mongodb.AuthenticationMechanism;
import com.mongodb.MongoClientURI;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.connect.errors.ConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MongoDbSinkConnectorConfig extends AbstractConfig {

    public static final String MONGODB_URI_SCHEME = "mongodb://";
    public static final String MONGODB_HOST_DEFAULT = "localhost";
    public static final int MONGODB_PORT_DEFAULT = 27017;
    public static final boolean MONGODB_AUTH_ACTIVE_DEFAULT = false;
    public static final String MONGODB_AUTH_MODE_DEFAULT = "SCRAM-SHA-1";
    public static final String MONGODB_AUTH_DB_DEFAULT = "admin";
    public static final String MONGODB_USERNAME_DEFAULT = "";
    public static final String MONGODB_PASSWORD_DEFAULT = "";
    public static final String MONGODB_DATABASE_DEFAULT = "kafkaconnect";
    public static final String MONGODB_COLLECTION_DEFAULT = "kafkatopic";
    public static final String MONGODB_WRITECONCERN_DEFAULT = "1";

    public static final String MONGODB_HOST_CONF = "mongodb.host";
    private static final String MONGODB_HOST_DOC = "single mongod host to connect with";

    public static final String MONGODB_PORT_CONF = "mongodb.port";
    private static final String MONGODB_PORT_DOC = "port mongod is listening on";

    public static final String MONGODB_AUTH_ACTIVE_CONF = "mongodb.auth.active";
    private static final String MONGODB_AUTH_ACTIVE_DOC = "whether or not the connection needs authentication";

    public static final String MONGODB_AUTH_MODE_CONF = "mongodb.auth.mode";
    private static final String MONGODB_AUTH_MODE_DOC = "which authentication mechanism is used";

    public static final String MONGODB_AUTH_DB_CONF = "mongodb.auth.db";
    private static final String MONGODB_AUTH_DB_DOC = "authentication database to use";

    public static final String MONGODB_USERNAME_CONF = "mongodb.username";
    private static final String MONGODB_USERNAME_DOC = "username for authentication";

    public static final String MONGODB_PASSWORD_CONF = "mongodb.password";
    private static final String MONGODB_PASSWORD_DOC = "password for authentication";

    public static final String MONGODB_DATABASE_CONF = "mongodb.database";
    private static final String MONGODB_DATABASE_DOC = "sink database name to write to";

    public static final String MONGODB_COLLECTION_CONF = "mongodb.collection";
    private static final String MONGODB_COLLECTION_DOC = "single sink collection name to write to";

    public static final String MONGODB_WRITECONCERN_CONF = "mongodb.writeconcern";
    private static final String MONGODB_WRITECONCERN_DOC = "write concern to apply when saving data";

    private static Logger logger = LoggerFactory.getLogger(MongoDbSinkConnectorConfig.class);

    public MongoDbSinkConnectorConfig(ConfigDef config, Map<String, String> parsedConfig) {
        super(config, parsedConfig);
    }

    public MongoDbSinkConnectorConfig(Map<String, String> parsedConfig) {
        this(conf(), parsedConfig);
    }

    public static ConfigDef conf() {
        return new ConfigDef()
                .define(MONGODB_HOST_CONF, Type.STRING, MONGODB_HOST_DEFAULT, Importance.HIGH, MONGODB_HOST_DOC)
                .define(MONGODB_PORT_CONF, Type.INT, MONGODB_PORT_DEFAULT, ConfigDef.Range.between(0,65536), Importance.HIGH, MONGODB_PORT_DOC)
                .define(MONGODB_AUTH_ACTIVE_CONF, Type.BOOLEAN, MONGODB_AUTH_ACTIVE_DEFAULT, Importance.MEDIUM, MONGODB_AUTH_ACTIVE_DOC)
                .define(MONGODB_AUTH_MODE_CONF, Type.STRING, MONGODB_AUTH_MODE_DEFAULT, ConfigDef.ValidString.in(MONGODB_AUTH_MODE_DEFAULT), Importance.MEDIUM, MONGODB_AUTH_MODE_DOC)
                .define(MONGODB_AUTH_DB_CONF, Type.STRING, MONGODB_AUTH_DB_DEFAULT, Importance.MEDIUM, MONGODB_AUTH_DB_DOC)
                .define(MONGODB_USERNAME_CONF, Type.STRING, MONGODB_USERNAME_DEFAULT, Importance.MEDIUM, MONGODB_USERNAME_DOC)
                .define(MONGODB_PASSWORD_CONF, Type.PASSWORD, MONGODB_PASSWORD_DEFAULT, Importance.MEDIUM, MONGODB_PASSWORD_DOC)
                .define(MONGODB_DATABASE_CONF, Type.STRING, MONGODB_DATABASE_DEFAULT, Importance.HIGH, MONGODB_DATABASE_DOC)
                .define(MONGODB_COLLECTION_CONF, Type.STRING, MONGODB_COLLECTION_DEFAULT, Importance.HIGH, MONGODB_COLLECTION_DOC)
                .define(MONGODB_WRITECONCERN_CONF, Type.STRING, MONGODB_WRITECONCERN_DEFAULT, Importance.HIGH, MONGODB_WRITECONCERN_DOC)
                ;
    }

    public MongoClientURI buildClientURI() {

        String hostAndPort = getString(MONGODB_HOST_CONF)+":"+getInt(MONGODB_PORT_CONF)+"/";

        StringBuilder sb = new StringBuilder();
        sb.append(MONGODB_URI_SCHEME);

        if(getBoolean(MONGODB_AUTH_ACTIVE_CONF)) {
            logger.debug("authentication active");

            if(!AuthenticationMechanism.SCRAM_SHA_1.getMechanismName()
                    .equals(getString(MONGODB_AUTH_MODE_CONF))) {
                throw new ConnectException("error currently only "+AuthenticationMechanism.SCRAM_SHA_1+" supported");
            }

            if(getString(MONGODB_USERNAME_CONF).isEmpty()
                    || getPassword(MONGODB_PASSWORD_CONF).value().isEmpty()) {
                throw new ConnectException("error missing credentials - username/password not specified");
            }
            sb.append(getString(MONGODB_USERNAME_CONF)).append(":")
                .append(getPassword(MONGODB_PASSWORD_CONF).value()).append("@")
                .append(hostAndPort).append(getString(MONGODB_DATABASE_CONF))
                .append("?authSource=").append(getString(MONGODB_AUTH_DB_CONF))
                .append("&w=").append(getString(MONGODB_WRITECONCERN_CONF));
        } else {
            logger.debug("authentication not active");
            sb.append(hostAndPort).append(getString(MONGODB_DATABASE_CONF))
                .append("?w=").append(getString(MONGODB_WRITECONCERN_CONF));
        }

        String uri = sb.toString();
        logger.debug("returning MongoClientURI for {}",uri);
        return new MongoClientURI(uri);

    }

}