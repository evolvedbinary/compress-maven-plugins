# Compress/Uncompress Maven Plugins

[![Build Status](https://dl.circleci.com/status-badge/img/gh/evolvedbinary/compress-maven-plugins/tree/main.svg?style=svg)](https://dl.circleci.com/status-badge/redirect/gh/evolvedbinary/compress-maven-plugins/tree/main)
[![Java 8](https://img.shields.io/badge/java-8-blue.svg)](https://adoptopenjdk.net/)
[![License](https://img.shields.io/badge/license-BSL%201.1-blue.svg)](https://spdx.org/licenses/BUSL-1.1.html)
[![Maven Central](https://img.shields.io/maven-central/v/com.evolvedbinary.maven.plugins/compress-maven-plugins?logo=apachemaven&label=maven+central&color=green)](https://central.sonatype.com/search?namespace=com.evolvedbinary.maven.plugins)

Maven plugins to compress and uncompress various archive formats.

There are 3 plugins available:
 1. `com.evolvedbinary.maven.plugins:compress-uncompress-maven-plugin`
 2. `com.evolvedbinary.maven.plugins:compress-maven-plugin`
 3. `com.evolvedbinary.maven.plugins:uncompress-maven-plugin`

 The first plugin offers both `compress` and `uncompress` goals. The other two plugins each offer only `compress` and `uncompress` goals respectively. If you are not sure, you probably want he first plugin, the other two plugins are a courtesy to allow you to separate the compress/uncompress tasks for easier reading within your `pom.xml` if you prefer.

## Example Uncompression Use

For example if you wanted to unzip the file `src/main/resources/folder1.zip` to the folder `target/folder1`:

```xml
<plugin>
    <groupId>com.evolvedbinary.maven.plugins</groupId>
    <artifactId>compress-uncompress-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>uncompress-zip</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>uncompress</goal>
            </goals>
            <configuration>
                <inputFile>${project.basedir}/src/main/resources/folder1.zip</inputFile>
                <outputPath>${project.build.directory}/folder1</outputPath>
                <algorithm>zip</algorithm>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Example Compression Use

For example if you wanted to zip the folder `src/main/resources/folder1` into the file `target/folder1.zip`:
```xml
<plugin>
    <groupId>com.evolvedbinary.maven.plugins</groupId>
    <artifactId>compress-uncompress-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        <execution>
            <id>compress-dir-zip</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>compress</goal>
            </goals>
            <configuration>
                <inputPath>${project.basedir}/src/main/resources/folder1</inputPath>
                <outputPath>${project.build.directory}/folder1.zip</outputPath>
                <algorithm>zip</algorithm>
                <compressionLevel>4</compressionLevel>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Supported Algorithms
    * Zip

If you want to add your own algoirith, you just need to implement `com.evolvedbinary.maven.plugins.compressuncompress.common.compression.api.CompressionProvider` and/or `com.evolvedbinary.maven.plugins.compressuncompress.common.uncompression.api.UncompressionProvider`, and make them discoverable to the <a href="https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html">Java Service Loader</a>. You then make your classes available to the plugin by adding them as a dependency, for example:

```xml
<plugin>
    <groupId>com.evolvedbinary.maven.plugins</groupId>
    <artifactId>compress-uncompress-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
        
        ...

    </executions>
    <dependencies>
        <dependency>
            <groupId>com.my.organisation</groupId>
            <artifactId>my-compression-provider</artifactId>
            <version>my-version-number</version>
        </dependency>
    </dependencies>
</plugin>
```