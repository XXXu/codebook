package org.example.serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Student implements Serializable {
    private String name;
    private int age;
    private String email;

    private Phone phone;

    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
    public static Student byteToObject(byte[] bytes) {
        Student stu = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            stu = (Student) oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("translation " + e.getMessage());
            e.printStackTrace();
        }
        return stu;
    }

    public static byte[] objectToByte(Student stu) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(stu);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("translation " + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }

    public static void main(String[] args) throws IOException {
        Student student = new Student("啊实打实的", 23);
        Phone phone = new Phone();
        phone.setQuhao(12);
        Set<String> num = new HashSet<>();
        num.add("asd");
        num.add("jiji");
        phone.setNumbers(num);
        student.setPhone(phone);
        ObjectMapper mapper = new ObjectMapper();
        String s = mapper.writeValueAsString(student);
        System.out.println(s);

        /*try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/root/serializable/student1.txt"))) {
            oos.writeObject(student);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("/root/serializable/student.txt"))) {
            Student brady = (Student) ois.readObject();
            System.out.println(brady);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        /*OutputStream outputStream3 = new FileOutputStream(new File("/root/serializable/student3.txt"));
        outputStream3.write(objectToByte(student));
        outputStream3.close();

        ObjectMapper mapper = new ObjectMapper();

        OutputStream outputStream2 = new FileOutputStream(new File("/root/serializable/student2.txt"));
        outputStream2.write(mapper.writeValueAsString(student).getBytes());
        outputStream2.close();*/

//        Student student1 = byteToObject(bytes);
//        System.out.println(student1);
    }
}
@JsonInclude(JsonInclude.Include.NON_NULL)
class Phone {
    private int quhao;
    private Set<String> numbers;

    public int getQuhao() {
        return quhao;
    }

    public void setQuhao(int quhao) {
        this.quhao = quhao;
    }

    public Set<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(Set<String> numbers) {
        this.numbers = numbers;
    }
}
