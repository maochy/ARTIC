package artic.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Resource resolver for files under the project root or classpath.
 */
public final class ProjectResource {
    private static final String[] RESOURCE_ROOTS = {
            "",
            "ARTIC/"
    };

    private ProjectResource() {
    }

    public static BufferedReader openReader(String relativePath) throws IOException {
        return new BufferedReader(new InputStreamReader(openStream(relativePath), StandardCharsets.UTF_8));
    }

    public static InputStream openStream(String relativePath) throws IOException {
        for (String root : RESOURCE_ROOTS) {
            File file = new File(root + relativePath);
            if (file.isFile()) {
                return new FileInputStream(file);
            }
        }

        InputStream stream = ProjectResource.class.getClassLoader().getResourceAsStream(relativePath);
        if (stream != null) {
            return stream;
        }
        throw new FileNotFoundException("Resource not found: " + relativePath);
    }

    public static File resolveFile(String relativePath) throws IOException {
        for (String root : RESOURCE_ROOTS) {
            File file = new File(root + relativePath);
            if (file.isFile()) {
                return file;
            }
        }

        URL url = ProjectResource.class.getClassLoader().getResource(relativePath);
        if (url == null) {
            throw new FileNotFoundException("Resource not found: " + relativePath);
        }
        if ("file".equals(url.getProtocol())) {
            try {
                return new File(url.toURI());
            } catch (URISyntaxException e) {
                throw new IOException("Invalid resource URI: " + relativePath, e);
            }
        }

        String fileName = new File(relativePath).getName();
        String prefix = fileName;
        int dot = fileName.lastIndexOf('.');
        String suffix = "";
        if (dot >= 0) {
            prefix = fileName.substring(0, dot);
            suffix = fileName.substring(dot);
        }
        if (prefix.length() < 3) {
            prefix = (prefix + "___").substring(0, 3);
        }

        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(temp)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
        }
        return temp;
    }
}
