# scala-singapore-august

This is an example on how to do chunked file upload in Spray.io.

There are two flavors for the API, one is `Actor` based model and the other `Future` based.

The `Future` based API is arguably simpler to use but requires to set a sensible timeout value which is tricky especially if we do not know the typical size of files being that the server will receive. With `Actor` API there is no such limitation.

__Running__

Fire the server from the command line - `sbt run`

Try to upload file the the existing endpoints, e.g. with curl, `curl -vX POST 127.0.0.1:8080/upload --data-binary @path/to/file --limit-rate 1m`
