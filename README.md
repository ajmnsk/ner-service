Named entity recognition (micro)service sample using Akka HTTP
============================================================

This project is inspired by [Akka HTTP microservice example from lightbend](http://www.lightbend.com/activator/template/akka-http-microservice),
and uses [simple Named entity recognition](https://github.com/ajmnsk/ner) library.

## Additional level of indirection

This implementation uses ```ActorEventBus``` with class based lookup classification.
All incoming requests are published onto event bus, and picked up by a designated highest level ```EventManager``` Actor.
```EventManager```, subsequently, forwards the message to a corresponding ```ClientManager``` actor.
ClientManager(s) are created on per user / client id basis. and maintaned as ```EventManager``` collection of children.
User / client id is a part of each incoming request,
So, if required, ```ClientManager``` actor could be used to maintain / preserve a client specific state / logic.

## Using Scala Routing for a pool of workers

Also, each ```ClientManger``` actor is provided with a reference to a ```nerWorkersRouter```.
So, the actual work is executed by a stateless ```NerWorker``` actor from  a pool.

## Non-blocking execution

Results of execution, success or failure are assigned to a ```Promise``` object, which is attached to each message.

The project shows the following tasks:

* using ActorEventBus for indirection,
* using Scala Routing for a pool of workers,
* non-blocking execution using Promise,
* testing.

The service provides one REST endpoint.

## Usage

To use the service, [ner-assembly-0.0.1-SNAPSHOT.jar](https://github.com/ajmnsk/ner) library needs to be included into project.

Start services with sbt:

```
$ sbt
> ~re-start
```

With the service up, you can start sending HTTP requests:

```
curl -X GET -H 'Content-Type: application/json' http://localhost:9000/ner/herid -d "{\"text\": \"Marcora doesn't even have to talk in his mother tongue to spark a reaction: In his adopted hometown of Chatham in Kent, southeast of London, just speaking English with an Italian accent can be enough to provoke a reaction. This is post-Brexit referendum Britain. And it's a place Marcora, who has lived and worked in the UK for 18 years, barely recognizes.\"}"
curl -X GET -H 'Content-Type: application/json' http://localhost:9000/ner/myid -d "{\"tagsToCollect\":[\"LOCATION\"],\"text\": \"Marcora doesn't even have to talk in his mother tongue to spark a reaction: In his adopted hometown of Chatham in Kent, southeast of London, just speaking English with an Italian accent can be enough to provoke a reaction. This is post-Brexit referendum Britain. And it's a place Marcora, who has lived and worked in the UK for 18 years, barely recognizes.\"}"
```

### Testing

Execute tests using `test` command:

```
$ sbt
> test
```

## Author & license

If you have any questions regarding this project contact:
Andrei <ajmnsk@gmail.com>
For licensing info see LICENSE file in project's root directory.
