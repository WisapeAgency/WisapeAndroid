/*
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
*/
cordova.define('cordova/plugin_list', function(require, exports, module) {
    module.exports = [
        {
            "file": "plugins/cordova.plugin.file/www/File.js",
            "id": "cordova.plugin.file.File",
            "merges": [
                "File"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/DirectoryEntry.js",
            "id": "cordova.plugin.file.DirectoryEntry",
            "merges": [
                "DirectoryEntry"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/DirectoryReader.js",
            "id": "cordova.plugin.file.DirectoryReader",
            "merges": [
                "DirectoryReader"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/Entry.js",
            "id": "cordova.plugin.file.Entry",
            "merges": [
                "Entry"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileEntry.js",
            "id": "cordova.plugin.file.FileEntry",
            "merges": [
                "FileEntry"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileError.js",
            "id": "cordova.plugin.file.FileError",
            "merges": [
                "FileError"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileReader.js",
            "id": "cordova.plugin.file.FileReader",
            "merges": [
                "FileReader"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileSystem.js",
            "id": "cordova.plugin.file.FileSystem",
            "merges": [
                "FileSystem"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/fileSystemPaths.js",
            "id": "cordova.plugin.file.fileSystemPaths",
            "merges": [
                "fileSystemPaths"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/fileSystems.js",
            "id": "cordova.plugin.file.fileSystems",
            "merges": [
                "fileSystems"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/fileSystems-roots.js",
            "id": "cordova.plugin.file.fileSystems-roots",
            "merges": [
                "fileSystems-roots"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileUploadOptions.js",
            "id": "cordova.plugin.file.FileUploadOptions",
            "merges": [
                "FileUploadOptions"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileUploadResult.js",
            "id": "cordova.plugin.file.FileUploadResult",
            "merges": [
                "FileUploadResult"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/FileWriter.js",
            "id": "cordova.plugin.file.FileWriter",
            "merges": [
                "FileWriter"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/Flags.js",
            "id": "cordova.plugin.file.Flags",
            "merges": [
                "Flags"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/LocalFileSystem.js",
            "id": "cordova.plugin.file.LocalFileSystem",
            "merges": [
                "LocalFileSystem"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/Metadata.js",
            "id": "cordova.plugin.file.Metadata",
            "merges": [
                "Metadata"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/ProgressEvent.js",
            "id": "cordova.plugin.file.ProgressEvent",
            "merges": [
                "ProgressEvent"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/requestFileSystem.js",
            "id": "cordova.plugin.file.requestFileSystem",
            "merges": [
                "requestFileSystem"
            ]
        },{
            "file": "plugins/cordova.plugin.file/www/resolveLocalFileSystemURI.js",
            "id": "cordova.plugin.file.resolveLocalFileSystemURI",
            "merges": [
                "resolveLocalFileSystemURI"
            ]
        }
    ];
});
