# 详解protobuf-从原理到使用
先看官网介绍：
***
他是一种与语言无关、与平台无关，是一种可扩展的用于序列化和结构化数据的方法，常用于用于通信协议，数据存储等。
他是一种灵活，高效，自动化的机制，用于序列化结构化数据，对比于 XML，他更小，更快，更简单。
***
最简单粗暴的理解方式，就是结合 JSON 和 XML 来理解，你可以暂时将他们仨理解成同一种类型的事物，但是呢，Protobuf 对比于他们两个，拥有着体量更小，解析速度更快的优势。

## 定义message结构
protobuf将一种结构称为一个message类型，我们以电话簿中的数据为例：
```
message Person {
  required string name = 1;
  required int32 id = 2; [default = 0]
  optional string email = 3;

  repeated int32 samples = 4 [packed=true];

}
```
其中Person是message这种结构的名称，name、id、email是其中的Field，每个Field保存着一种数据类型，后面的1、2、3是Filed对应的数字id。id在1(15之间编码只需要占一个字节，包括Filed
数据类型和Filed对应数字id，在16)2047之间编码需要占两个字节，所以最常用的数据对应id要尽量小一些。后面具体讲到编码规则时会细讲。
Field最前面的required,optional,repeated是这个Filed的规则，分别表示该数据结构中这个Filed有且只有1个，可以是0个或1个，可以是0个或任意个。optional后面可以加default默认值，如果不加，数据类型的默认为0，字符串类型的默认为空串。
repeated后面加[packed=true]会使用新的更高效的编码方式。注意：使用required规则的时候要谨慎，因为以后结构若发生更改，这个Filed若被删除的话将可能导致兼容性的问题。
### 保留Field和保留Field number
每个Field对应唯一的数字id，但是如果该结构在之后的版本中某个Field删除了，为了保持向前兼容性，需要将一些id或者名称设置为保留的，即不能被用来定义新的Field。
```
message Person {
  reserved 2, 15, 9 to 11;
  reserved "samples", "email";
}
```
### 枚举类型
比如电话号码，只有移动电话、家庭电话、工作电话三种，因此枚举作为选项，如果没设置的话，枚举类型的默认值为第一项。在上面的例子中在个人message中加入电话号码这个Field。如果枚举类型中有不同的名字对应相同的数字id
，需要加入option allow_alias=true这一项，否则会报错。枚举类型中也有reserverd Field和number，定义和message中一样。
```
message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;

  enum PhoneType {
    //allow_alias = true;
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  message PhoneNumber {
    required string number = 1;
    optional PhoneType type = 2 [default = HOME];
  }

  repeated PhoneNumber phones = 4;
}
```
### 引用其它message类
在同一个文件中，可以直接引用定义过的message类型。在同一个项目中，可以用import来导入其它message类型。
```
import "myproject/other_protos.proto";
```
或者在一个message类型中嵌套定义其它的message类型
### message拓展
```
message Person {
  // ...
  extensions 100 to 199;
}
```
在另一个文件中，import这个proto之后，可以对Person这个message进行拓展。
```
extend Person {
  optional int32 bar = 126;
}
```
## 数据类型对应关系
proto中数据类型和c++，python，java中数据类型对应规则如下，更多的语言请参考官网：

| .proto | C++ | Python | Java | 介绍 | 
| :---         |     :---      |          :--- |   :--- |:--- |
| double | double | float | double |
|  float  | float    |  float | float |
|  int32  | int32    |  int | int | 可变长编码，对负数效率不高 |
|  int64  | int64    |  int/long | long |
|  uint32  | uint32    |  int/long | int |
|  uint64  | uint64    |  ing/long | long |
|  sint32  | int32    |  int | int | 可变长编码，对负数效率较高 |
|  sint64  | int64    |  int/long | long |
|  fixed32  | uint32    |  int/long | int | 32位定长编码 |
|  fixed64  | uint64    |  int/long | long |
|  sfixed32  | int32    |  int | int |
|  sfixed64  | int64    |  int/long | long |
|  bool  | bool    |  bool | boolean |
|  string  | string    |  str/unicode | String | UTF-8编码或者7-ASCII编码 |
|  bytes  | string    |  str | ByteString |
 
## 编码规则
protobuf有一套高效的数据编码规则
### 可变长整数编码
每个字节有8bits，其中第一个bit是most significant bit(msb)，0表示结束，1表示还要读接下来的字节。
对message中每个Field来说，需要编码它的数据类型、对应id以及具体数据。
数据类型有以下种，可以用3个bits表示，每个整数编码用最后3个bits表示数据类型。所以，对应id在1~15之间的Field，可以用1个字节编码数据类型、对应id。


| Type | Meaning | Used For |
| :---         |     :---      |          :--- |
| 0 | Varint | int32, int64, uint32, uint64, sint32, sint64, bool, enum |
| 1 | 64-bit | fixed64, sfixed64, double |
| 2 | Length-delimited	 | string, bytes, embedded messages, packed repeated fields |
| 3 | Start group | groups (deprecated) |
| 4 | End group | groups (deprecated) |
| 5 | 32-bit | fixed32, sfixed32, float |

比如对于下面这个例子来说，如果给a赋值150，那么最终得到的编码是什么呢？
```
message Test {
  optional int32 a = 1;
}
```
首先数据类型编码是000，因此和id联合起来的编码是00001000. 然后值150的编码是1 0010110，采用小端序交换位置，即0010110 0000001，前面补1后面补0，即10010110 00000001，即96 01，加上最前面的数据类型编码字节，总的编码为08 96 01。

### 有符号整数编码
如果用int32来保存一个负数，结果总是有10个字节长度，被看作是一个非常大的无符号整数。使用有符号类型会更高效，它使用一种Zigzag的方式进行编码，即-1编码成1，1编码成2，-2编码成3这种形式。也就是说，对于sint32
来说，n编码成(n<<1)^(n>>31),注意到第二个移位是算法移位。

### 定长编码
定长编码是比较简单的情况。

## 安装protobuf包

## Java测试代码
```
syntax = "proto2";

package tutorial;

message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;

  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  message PhoneNumber {
    required string number = 1;
    optional PhoneType type = 2 [default = HOME];
  }

  repeated PhoneNumber phones = 4;
}

message AddressBook {
  repeated Person people = 1;
}
```
编码成java文件，进入 proto 文件所在路径，输入下面 protoc 命令（后面有三部分参数），然后将编译得出的 java 文件拷贝到项目中即可（此 java 文件可以理解成使用的数据对象）：
```
protoc -I=./ --java_out=./ ./JetProtos.proto
 或
protoc -proto_path=./ --java_out=./ ./JetProtos.proto
```
***
参数说明：
1.-I 等价于 -proto_path：指定 .proto 文件所在的路径
2.--java_out：编译成 java 文件时，标明输出目标路径
3../JetProtos.proto：指定需要编译的 .proto 文件
***

### mavan引入指定包
```
<dependency>     
    <groupId>com.google.protobuf</groupId>     
    <artifactId>protobuf-java</artifactId>     
    <version>2.5.0</version>
</dependency>
```
### 使用
序列化和反序列化有多种方式，可以是byte[],也可以是inputStream等：

```
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import tutorial.Addressbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProtoTest {

    public static void main(String[] args) {
        try {
            /** Step1：生成 AddressBook 对象 */
            Addressbook.AddressBook.Builder addressBookBuilder = Addressbook.AddressBook.newBuilder();
            Addressbook.Person.Builder personBuilder = Addressbook.Person.newBuilder();
            personBuilder.setId(1);
            personBuilder.setName("koma");
            personBuilder.setEmail("xiaoxiao.xu@transwarp.io");

            Addressbook.Person.PhoneNumber.Builder phoneNumber = Addressbook.Person.PhoneNumber.newBuilder();
            phoneNumber.setNumber("13888888888");
            phoneNumber.setType(Addressbook.Person.PhoneType.HOME);

            personBuilder.addPhones(phoneNumber);
            addressBookBuilder.addPeople(personBuilder);


            System.out.println(personBuilder.build());
            System.out.println("---------------------");

            System.out.println(addressBookBuilder);

            /** Step2：序列化和反序列化 */
            // 方式一 byte[]：
            // 序列化
            byte[] bytes = personBuilder.build().toByteArray();
            // 反序列化
            Addressbook.Person person = Addressbook.Person.parseFrom(bytes);

            System.out.println("##"+person);

            // 方式二 ByteString：
            // 序列化
            ByteString byteString = personBuilder.build().toByteString();
            System.out.println("string: "+byteString.toString());
            // 反序列化
            Addressbook.Person person1 = Addressbook.Person.parseFrom(byteString);
            System.out.println("person1: "+person1);

            // 方式三 InputStream
            // 粘包,将一个或者多个protobuf 对象字节写入 stream
            // 序列化
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            personBuilder.build().writeDelimitedTo(byteArrayOutputStream);

            // 反序列化，从 steam 中读取一个或者多个 protobuf 字节对象
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            Addressbook.Person person2 = Addressbook.Person.parseDelimitedFrom(byteArrayInputStream);
            System.out.println("person2: "+person2);

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

    }

}
```




