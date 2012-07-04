package org.forgerock.openam.session.ha.amsessionstore.i18n;



import org.forgerock.i18n.LocalizableMessageDescriptor;



/**
 * This file contains localizable message descriptors having the resource
 * name {@code org.forgerock.openam.session.ha.i18n.amsessionstore}. This file was generated
 * automatically by the {@code i18n-maven-plugin} from the property file
 * {@code org/forgerock/openam/session/ha/amsessionstore/i18n/amsessionstore.properties} and it should not be manually edited.
 */
public final class AmsessionstoreMessages
{
  // The name of the resource bundle.
  private static final String RESOURCE = "org.forgerock.openam.session.ha.amsessionstore.i18n.amsessionstore";

  // Prevent instantiation.
  private AmsessionstoreMessages()
  {
    // Do nothing.
  }
  
  /**
   * Returns the name of the resource associated with the messages contained
   * in this class. The resource name may be used for obtaining named loggers,
   * e.g. using SLF4J's {@code org.slf4j.LoggerFactory#getLogger(String name)}.
   *
   * @return The name of the resource associated with the messages contained
   *         in this class.
   */
  public String resourceName()
  {
    return RESOURCE;
  }

  /**
   * Internal Shutdown called
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_AM_INT_SHUT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_AM_INT_SHUT",-1);

  /**
   * shutdown called
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_AM_SHUT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_AM_SHUT",-1);

  /**
   * OpenDJ shutdown failure
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_AM_SHUT_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_AM_SHUT_FAIL",-1);

  /**
   * complete.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_COMPLETE =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_COMPLETE",-1);

  /**
   * Unable to delete entry: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DEL_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DEL_FAIL",-1);

  /**
   * Configuring First Server
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_CONF_FIR =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_CONF_FIR",-1);

  /**
   * Loading amsessiondb ldif %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_LD =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_LD",-1);

  /**
   * amsessiondb ldif loading failed
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_LD_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_LD_FAIL",-1);

  /**
   * amsessiondb ldif loading, success
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_LD_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_LD_OK",-1);

  /**
   * Unable to start embedded OpenDJ server.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_NO_START =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_NO_START",-1);

  /**
   * Unable to start embedded OpenDJ server %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_NO_START2 =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_NO_START2",-1);

  /**
   * Unable to parse date %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_PARSE =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_PARSE",-1);

  /**
   * EmbeddedOpenDJ.setup(): Error copying schema file(s)
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SCH_ERR =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SCH_ERR",-1);

  /**
   * Starting OpenDJ setup
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SETUP =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP",-1);

  /**
   * EmbeddedOpenDJ.setupOpenDJ. Error setting up OpenDJ %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_SETUP_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_FAIL",-1);

  /**
   * OpenDJ setup failed.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SETUP_FAIL2 =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_FAIL2",-1);

  /**
   * OpenDJ setup failed %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_SETUP_FAIL3 =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_FAIL3",-1);

  /**
   * OpenDJ setup successfully.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SETUP_FIN =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_FIN",-1);

  /**
   * OpenDJ setup complete
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SETUP_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_OK",-1);

  /**
   * Running OpenDJ Setup command %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_SETUP_RUN =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SETUP_RUN",-1);

  /**
   * EmbeddedOpenDJ.shutdown server...
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SHUT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SHUT",-1);

  /**
   * EmbeddedOpenDJ shutdown server success.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SHUT_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SHUT_OK",-1);

  /**
   * Starting OpenDJ Setup
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_START =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_START",-1);

  /**
   * Start Embedded OpenDJ server %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_STARTING =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_STARTING",-1);

  /**
   * EmbeddedOpenDJ.startServer:starting DJ Server...
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_STARTING1 =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_STARTING1",-1);

  /**
   * ...EmbeddedOpenDJ.startServer:DJ Server started.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_STARTING2 =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_STARTING2",-1);

  /**
   * ...EmbeddedOpenDJ.startServer:DJ Server not started.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_START_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_START_FAIL",-1);

  /**
   * OpenDJPersistentStore created successfully.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_STR_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_STR_OK",-1);

  /**
   * EmbeddedOpenDJ.setupOpenDJ. Error loading amsessiondb suffix %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_DJ_SUF_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SUF_FAIL",-1);

  /**
   * amsessiondb suffix created successfully.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SUF_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SUF_OK",-1);

  /**
   * Unable to determine server count
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SVR_CNT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SVR_CNT",-1);

  /**
   * EmbeddedOpenDJ.setup(): Error tag swapping files
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_SWP_ERR =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_SWP_ERR",-1);

  /**
   * Error copying zip contents
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_DJ_ZIP_ERR =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_DJ_ZIP_ERR",-1);

  /**
   * Error in accessing entry DN: %s, error code = %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_ENT_ACC_FAIL =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_ACC_FAIL",-1);

  /**
   * Error in accessing entry DN: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_ENT_ACC_FAIL2 =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_ACC_FAIL2",-1);

  /**
   * Unable to delete entry: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_ENT_DEL_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_DEL_FAIL",-1);

  /**
   * Error in deleting expired records
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_ENT_EXP_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_EXP_FAIL",-1);

  /**
   * Unable to delete %s not found
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_ENT_NOT_FOUND =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_NOT_FOUND",-1);

  /**
   * Entry not present: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_ENT_NOT_P =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_NOT_P",-1);

  /**
   * Entry present: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_ENT_P =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_ENT_P",-1);

  /**
   * Get Record Count %s found %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_GET_REC_CNT_OK =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_GET_REC_CNT_OK",-1);

  /**
   * Host 1 %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_H_A =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_H_A",-1);

  /**
   * Host 2 %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_H_B =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_H_B",-1);

  /**
   * Invalid map attr: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_INV_MAP =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_INV_MAP",-1);

  /**
   * Malformed URL: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_MAL_URL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_MAL_URL",-1);

  /**
   * Session was not found: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_MEM_SES_EX =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_MEM_SES_EX",-1);

  /**
   * Configured Persistent Store: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_PER_CONF =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_PER_CONF",-1);

  /**
   * Created PersistentStoreManager instance
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_PER_CREATE =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_PER_CREATE",-1);

  /**
   * Unable to create PersistentStoreManager
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_PER_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_PER_FAIL",-1);

  /**
   * Error while converting property value: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_PROP_ERR =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_PROP_ERR",-1);

  /**
   * Port 1 %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_P_A =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_P_A",-1);

  /**
   * Port 2 %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_P_B =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_P_B",-1);

  /**
   * Removing replication server host: %s admin port: %s suffix: %s
   */
  public static final LocalizableMessageDescriptor.Arg3<CharSequence,CharSequence,CharSequence> DB_REPL_DEL =
          new LocalizableMessageDescriptor.Arg3<CharSequence,CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_DEL",-1);

  /**
   * OpenDJ replication failed to be disabled.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_DEL_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_DEL_FAIL",-1);

  /**
   * OpenDJ replication disabled successfully.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_DEL_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_DEL_OK",-1);

  /**
   * Replication Setup Failed.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_FAIL",-1);

  /**
   * Replication Setup Success.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_OK",-1);

  /**
   * replication setup succeeded.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_SETUP =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_SETUP",-1);

  /**
   * Error setting up replication: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_REPL_SETUP_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_SETUP_FAIL",-1);

  /**
   * OpenDJ replication setup failed.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_SETUP_FAILED =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_SETUP_FAILED",-1);

  /**
   * OpenDJ replication setup successfully.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_REPL_SETUP_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_REPL_SETUP_OK",-1);

  /**
   * Unable to delete %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_DEL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_DEL",-1);

  /**
   * Unable to delete expired %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_DEL_EXP =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_DEL_EXP",-1);

  /**
   * Unable to get record count %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_GRC =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_GRC",-1);

  /**
   * Unable to read %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_READ =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_READ",-1);

  /**
   * Unable to read with secondary key %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_SEC_KEY =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_SEC_KEY",-1);

  /**
   * Read with secondary key %s found %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_R_SEC_KEY_OK =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_SEC_KEY_OK",-1);

  /**
   * Unable to process write %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_R_WRITE =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_R_WRITE",-1);

  /**
   * amsessiondb is already installed and configured on this node
   * to reinstall, try removing the opendj directory and re-run this command
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SETUP_ALD =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SETUP_ALD",-1);

  /**
   * Unable to setup amsessiondb: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_SETUP_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SETUP_FAIL",-1);

  /**
   * "%s URL in amsessiondb.properties %s is invalid, exiting.
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_SETUP_URL =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SETUP_URL",-1);

  /**
   * Authentication not required
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SET_AUTH_NREQ =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SET_AUTH_NREQ",-1);

  /**
   * Authentication setup
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SET_AUTH_OK =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SET_AUTH_OK",-1);

  /**
   * amsessiondb is configured on this node
   * removing this node from the amsessiondb
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SET_CON_ND =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SET_CON_ND",-1);

  /**
   * amsessiondb is not configured on this host 
   * removal of this node is not possible.
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SET_CON_ND_NOT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SET_CON_ND_NOT",-1);

  /**
   * Unauthorized: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_SET_UNAUTH =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SET_UNAUTH",-1);

  /**
   * Shutdown amsessiondb called
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SHUT_CALL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_CALL",-1);

  /**
   * Unable to shutdown amsessiondb server
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SHUT_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_FAIL",-1);

  /**
   * Shutdown amsessiondb complete
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SHUT_FIN =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_FIN",-1);

  /**
   * Shutdown listener started on address %s and port %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_SHUT_LIST =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_LIST",-1);

  /**
   * Unauthorized access to amsessiondb server
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SHUT_NOAUTH =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_NOAUTH",-1);

  /**
   * Unable to shutdown persistent store
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SHUT_PER_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SHUT_PER_FAIL",-1);

  /**
   * Unable to close server socket
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SOCK_CLOSE =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SOCK_CLOSE",-1);

  /**
   * Unable to close socket resource
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SOCK_CLOSE_R =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SOCK_CLOSE_R",-1);

  /**
   * Unable to receive socket connection
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_SOCK_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_SOCK_FAIL",-1);

  /**
   * amsessiondb started on port %s with maximum threads %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_STARTED =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_STARTED",-1);

  /**
   * amsessiondb started with DIGEST authentication
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_STARTED_AUTH =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_STARTED_AUTH",-1);

  /**
   * amsessiondb started without authentication
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_STARTED_NOAUTH =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_STARTED_NOAUTH",-1);

  /**
   * Unable to start amsessiondb
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_START_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_START_FAIL",-1);

  /**
   * amsessiondb: shutdown,
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_START_MSG =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_START_MSG",-1);

  /**
   * Unable to determine store entry count: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_STATS_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_STATS_FAIL",-1);

  /**
   * Not a number: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_STATS_NFS =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_STATS_NFS",-1);

  /**
   * Unable to stop amsessiondb
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_STOP_FAIL =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_STOP_FAIL",-1);

  /**
   * Store Exception
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_STR_EX =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_STR_EX",-1);

  /**
   * Successfully created entry: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_SVR_CREATE =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SVR_CREATE",-1);

  /**
   * Unable to create: Entry Already Exists Error for DN %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_SVR_CRE_FAIL =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SVR_CRE_FAIL",-1);

  /**
   * Error creating entry: %s, error code = %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_SVR_CRE_FAIL2 =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SVR_CRE_FAIL2",-1);

  /**
   * Successfully modified entry: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_SVR_MOD =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SVR_MOD",-1);

  /**
   * Error modifying entry %s, error code = %s
   */
  public static final LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence> DB_SVR_MOD_FAIL =
          new LocalizableMessageDescriptor.Arg2<CharSequence,CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_SVR_MOD_FAIL",-1);

  /**
   * Thread interrupted
   */
  public static final LocalizableMessageDescriptor.Arg0 DB_THD_INT =
          new LocalizableMessageDescriptor.Arg0(AmsessionstoreMessages.class,RESOURCE,"DB_THD_INT",-1);

  /**
   * Unknown attribute: %s
   */
  public static final LocalizableMessageDescriptor.Arg1<CharSequence> DB_UNK_ATTR =
          new LocalizableMessageDescriptor.Arg1<CharSequence>(AmsessionstoreMessages.class,RESOURCE,"DB_UNK_ATTR",-1);

}
