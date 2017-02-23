/*
 * Mule ESB Maven Tools
 * <p>
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * <p>
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.tools.artifact.archiver.internal.packaging;

import com.google.common.collect.Iterables;
import org.mule.tools.artifact.archiver.internal.PackageBuilder;

import java.io.File;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.collect.Sets.newHashSet;

public enum PackagingType {

    SOURCES {
        @Override
        public Set<String> listDirectories() {
            return newHashSet(PackageBuilder.MULE_SRC_FOLDER);
        }

        @Override
        public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
            return packageBuilder.withMuleSrc(fileMap.get(PackageBuilder.MULE_SRC_FOLDER));
        }
    }, BINARIES {
        @Override
        public Set<String> listDirectories() {
            return newHashSet(PackageBuilder.MULE_FOLDER, PackageBuilder.CLASSES_FOLDER, PackageBuilder.REPOSITORY_FOLDER);
        }

        @Override
        public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
            return packageBuilder
                .withMule(fileMap.get(PackageBuilder.MULE_FOLDER))
                .withClasses(fileMap.get(PackageBuilder.CLASSES_FOLDER))
                .withRepository(fileMap.get(PackageBuilder.REPOSITORY_FOLDER));
        }
    }, BINARIES_AND_SOURCES {
        @Override
        public Set<String> listDirectories() {
            return newHashSet(Iterables.concat(SOURCES.listDirectories(), BINARIES.listDirectories()));

        }

        @Override
        public PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap) {
            return packageBuilder
                .withClasses(fileMap.get(PackageBuilder.CLASSES_FOLDER))
                .withMule(fileMap.get(PackageBuilder.MULE_FOLDER));
            //                .withMetaInf(fileMap.get(PackageBuilder.METAINF_FOLDER));
        }
    };

    public abstract Set<String> listDirectories();

    public abstract PackageBuilder applyPackaging(PackageBuilder packageBuilder, Map<String, File> fileMap);

    public static PackagingType fromString(String name) {
        String packagingName = LOWER_HYPHEN.to(LOWER_CAMEL, name);
        return valueOf(packagingName);
    }

}