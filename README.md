# SkProtobuf
Skript addon that adds support for [protobuf](https://protobuf.dev/).

[![SkriptHubViewTheDocs](http://skripthub.net/static/addon/ViewTheDocsButton.png)](http://skripthub.net/docs/?addon=SkProtobuf)

## Usage
SkProtobuf requires a descriptor set for the protobuf messages you wish to use with Skript. Use protoc's `--descriptor_set_out` option to generate a descriptor set:
```
-oFILE,                      Writes a FileDescriptorSet (a protocol buffer,
  --descriptor_set_out=FILE  defined in descriptor.proto) containing all of
                             input files to FILE.
```
Use the `--include_imports` option to create a self-contained descriptor set:
```
--include_imports  When using --descriptor_set_out, also include
                   all dependencies of the input files in the
                   set, so that the set is self-contained.
```
Place descriptor set files in the folder `plugins/SkProtobuf/descriptors` on your server.

## Examples
> [!TIP]
> You can reference messages by their fully qualified name or you may omit the package if the message name is unambiguous.

##### Proto
```protobuf
syntax = "proto3";

message Example {
  string foo = 1;
  repeated uint32 bar = 2;
}
```
##### Message Builder
```
set {_builder} to builder for proto message "Example"
set proto field "foo" in {_builder} to "hello world"
add 1, 2, and 3 to proto field "bar" in {_builder}
set {_message} to proto message from builder {_builder}
```
##### JSON
```
# parse from JSON string
set {_message} to "{""foo"": ""hello world"", bar: [1, 2, 3]}" parsed as proto message "Example"

# convert back to JSON string
set {_json} to "%{_message}%"
```
