package com.korwe.thecore.router;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

/**
 * Helper class to create SSLContext for router's standard ConnectionFactory.
 *
 * <p>The router uses standard RabbitMQ ConnectionFactory (not CoreConnectionFactory),
 * so this helper provides SSL configuration capabilities for the router.</p>
 *
 * @since 4.0.2
 * @see com.rabbitmq.client.ConnectionFactory#useSslProtocol(SSLContext)
 */
public class SslContextFactory {

    private String trustStorePath;
    private String trustStorePassword;
    private String sslProtocol = "TLSv1.2";

    /**
     * Creates an SSLContext with the configured trust store.
     *
     * @return configured SSLContext, or default SSLContext if no trust store configured
     * @throws Exception if SSL context creation fails
     */
    public SSLContext createSslContext() throws Exception {
        if (trustStorePath == null || trustStorePath.isEmpty()) {
            // No trust store configured, return default SSL context
            return SSLContext.getDefault();
        }

        // Load trust store
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(trustStorePath)) {
            trustStore.load(fis, trustStorePassword != null ?
                trustStorePassword.toCharArray() : null);
        }

        // Initialize trust manager
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
            TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance(sslProtocol);
        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

        return sslContext;
    }

    /**
     * Sets the path to the JKS trust store file.
     *
     * @param trustStorePath absolute path to trust store
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Sets the trust store password.
     *
     * @param trustStorePassword password for trust store
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Sets the SSL protocol version.
     *
     * @param sslProtocol SSL protocol ("TLSv1.2", "TLSv1.3", or "TLS")
     */
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }
}
