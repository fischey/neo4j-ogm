package org.neo4j.ogm.drivers.embedded;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

public class EmbeddedDriverWithAbsolutePathTest
{
  //from issue #179
    @Test
    public void test() {
        final String[] uris = new String[] {
                // URI shown in Firefox
                // => java.nio.file.InvalidPathException: Illegal char <:> at index 2: /D:/git/xom/core/temp/neo4j-junit/
                // This one should work!
                "file:///D:/temp/neo4j-junit/",
                // 2. test
                // => java.lang.IllegalArgumentException: URI has an authority component
                "file://D:/temp/neo4j-junit/",
                // URI when i use new File("d:/ ....).toURI().toString()
                // => java.nio.file.InvalidPathException: Illegal char <:> at index 2: /D:/git/xom/core/temp/neo4j-junit/
                // This one should work!
                "file:/D:/temp/neo4j-junit/",
                // 4. Test
                // => java.lang.IllegalArgumentException: URI is not hierarchical
                "file:D:/temp/neo4j-junit/" };

        for ( final String strPath : uris ) {
            createFileFromUriWithFile(strPath);
        }

        for ( final String strPath : uris ) {
          createFileFromUriWithPath(strPath);
        }
    }

    private void createFileFromUriWithFile(final String strPath)
    {
        try {
            final URI uri = new URI( strPath );
            final File file = new File( uri );
            if ( !file.exists() ) {
                final Path graphDir = Files.createDirectories( Paths.get( uri.getRawPath() ) );
            }
        }
        catch ( final Exception e ) {
            System.err.println( "File-URI: " + strPath );
            e.printStackTrace();
        }
    }
    private void createFileFromUriWithPath(final String strPath)
    {
        try {
            final URI uri = new URI( strPath );
            final File file = new File( uri );
            if ( !file.exists() ) {
                // Possible workaround:
                final Path graphDirWorking = Files.createDirectories( file.toPath() );
            }
        }
        catch ( final Exception e ) {
            System.err.println( "Path-URI: " + strPath );
            e.printStackTrace();
        }
    }
}