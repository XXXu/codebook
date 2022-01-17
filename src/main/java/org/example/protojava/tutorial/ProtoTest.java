package org.example.protojava.tutorial;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

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

            System.out.println(personBuilder);
            System.out.println("===========");

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
