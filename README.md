# file-upload-frontend

Frontend for uploading files to the Tax Platform

[![Build Status](https://travis-ci.org/hmrc/file-upload-frontend.svg?branch=master)](https://travis-ci.org/hmrc/file-upload-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/file-upload-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/file-upload-frontend/_latestVersion)

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application locally

To run the application execute

```
sbt run
```

The endpoints below can then be accessed with the base url http://localhost:8899/file-upload/

## Service manager

```
sm --start FILE_UPLOAD_ALL
```

## Endpoints

### Upload a File

This endpoint allows the uploading of a file to the tax platform by means of the `file-upload` service.

The calling service *must* ensure that:

* an envelope was defined via the `file-upload` service and an `envelopeId` and `fileId` are registered for that envelope
* the request contains the above valid `envelopeId` and `fileId`

i.e. If being invoked from an HTML form - the service generating the form should ensure that these fields are present
and have valid values assigned by the [`file-upload` (backend) service](https://github.com/hmrc/file-upload).

This endpoint *requires*:
 
* the contentType to be defined as `multipart/form-data` with the below parameters defined alongside the actual file content.
* a single filePart be present containing the data to be uploaded to the corresponding `fileId` within the envelope.

|---|---|
|HTTP method|POST|
|URL path|/upload|

#### Parameters
|Parameter|Required?|Example|Description|
|---|---|---|---|
|`envelopeId`|Y|`1234567890`|A file-upload service generated envelope identifier. This will be validated against the file-upload service so a valid envelope *must* have been created prior to invoking this endpoint|
|`fileId`|Y|`0987654321`|A file-upload service generated file identifier. This will be validated against the file-upload service so a valid envelope *must* have been created prior to invoking this endpoint|

#### Responses
|Outcome|Response Code|Definition|Parameters|Description|
|---|---|---|---|---|
|Success|200|`OK`|`None`|Returned if the file was uploaded successfully to the service|
|Failure|400|`BAD REQUEST`|`None`|If HTTP request of invalid format or missing mandatory parameters|

## Create Envelope (for test purposes only)

|---|---|
|HTTP method|POST|
|URL path|/test-only/create-envelope|

#### Example Response

``` JSON
{
  "envelopeId": "3f994068-f810-4290-89ca-7e8ec43cea9c"
}
```

## Show Envelope (for test purposes only)

Coming soon

## Show File (for test purposes only)

Coming soon

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")
