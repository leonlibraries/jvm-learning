package org.leon.methodhandles;

import org.junit.Test;

public class ByteCodeLearning
{

  @Test
  public void testConstruct()
  {
    Person person = new Person("Leon", "M");

    person.setGender("F");

    person.setName("Lee");

    Person.echo(person.getName(),"BBAB","CCAD");

  }

  public static void main(String[] args){

    System.out.println("HELLO");
  }

}


class Person
{

  private String name;

  private String gender;


  public static void echo(String name,String... abc)
  {
    System.out.println("Hello,"+name);
  }

  public Person(String name, String gender)
  {
    this.name = name;
    this.gender = gender;
  }

  public String getName()
  {
    return name;
  }

  public Person setName(String name)
  {
    this.name = name;
    return this;
  }

  public String getGender()
  {
    return gender;
  }

  public Person setGender(String gender)
  {
    this.gender = gender;
    return this;
  }
}
