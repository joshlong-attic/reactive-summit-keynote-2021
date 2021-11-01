ZIO Client
----------

```
./sbt "runMain MyServer"
```

```
curl 'http://localhost:8088/api/graphql' --data-binary '{"query":"query{\n employees(role: SoftwareDeveloper){\n name\n role\n}\n}"}'
```

```
./sbt "runMain MyClient"
```