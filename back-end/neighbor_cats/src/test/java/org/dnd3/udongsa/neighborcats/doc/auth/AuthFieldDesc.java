package org.dnd3.udongsa.neighborcats.doc.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

public class AuthFieldDesc {
  
  public static List<FieldDescriptor> getSignInReqDesc(){
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("email").type(JsonFieldType.STRING).description("로그인 이메일"));
    fields.add(fieldWithPath("password").type(JsonFieldType.STRING).description("비밀번호"));
    return fields;
  }

  public static List<FieldDescriptor> getSignInResDesc(){
    List<FieldDescriptor> fields = new ArrayList<>();
    fields.add(fieldWithPath("accessToken").type(JsonFieldType.STRING).description("인증 토큰"));
    return fields;
  }

public static List<FieldDescriptor> getMeResDesc() {
  List<FieldDescriptor> fields = new ArrayList<>();
  fields.add(fieldWithPath("id").type(JsonFieldType.NUMBER).description("식별자"));
  fields.add(fieldWithPath("name").type(JsonFieldType.STRING).description("실명"));
  fields.add(fieldWithPath("email").type(JsonFieldType.STRING).description("이메일"));
  fields.add(fieldWithPath("nickName").type(JsonFieldType.STRING).description("별명"));
  fields.add(fieldWithPath("address").type(JsonFieldType.STRING).description("주소지"));
  fields.add(fieldWithPath("phoneNumber").type(JsonFieldType.STRING).description("전화번호"));
  fields.add(fieldWithPath("roles[].id").type(JsonFieldType.NUMBER).description("Role 식별자"));
  fields.add(fieldWithPath("roles[].name").type(JsonFieldType.STRING).description("Role 명칭"));
  fields.add(fieldWithPath("cats[].id").type(JsonFieldType.NUMBER).description("Cat 식별자"));
  fields.add(fieldWithPath("cats[].name").type(JsonFieldType.STRING).description("Cat 이름"));
  fields.add(fieldWithPath("cats[].features").type(JsonFieldType.STRING).description("Cat 특징"));
  fields.add(fieldWithPath("cats[].kindName").type(JsonFieldType.STRING).description("Cat 품종"));
  fields.add(fieldWithPath("cats[].gender").type(JsonFieldType.STRING).description("Cat 성별"));
  fields.add(fieldWithPath("cats[].birthday").type(JsonFieldType.STRING).description("Cat 생일"));
  fields.add(fieldWithPath("cats[].neutralized").type(JsonFieldType.STRING).description("Cat 품종"));
  fields.add(fieldWithPath("cats[].profileImgUrl").type(JsonFieldType.VARIES).description("Cat 프로필 이미지 URL"));
  return fields;
}
}