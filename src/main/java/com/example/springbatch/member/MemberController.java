package com.example.springbatch.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/*
    Edit Configuration 에서 설정 복사 > VM 옵션 추가하여 아래 옵션 추가 > 실행 (웹용으로 띄우기 위함)
    -Dserver.port=8081

    스프링배치에서 외부 서비스를 호출한다는 가정이 필요하여 만든 컨트롤러 (외부 시스템에 해당)
 */
@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    @GetMapping
    public Page<Member> getMembers(@PageableDefault(page = 0, size = 10) Pageable pageable) {
        // 1~100까지 Member 생성
        List<Member> allMembers = IntStream.rangeClosed(1, 100)
                .mapToObj(i -> new Member((long) i, "Member " + i, "Member " + i + "@google.com"))
                .collect(Collectors.toList());

        // 페이지 범위 계산
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allMembers.size());

        // 페이지 객체 생성 후 리턴
        List<Member> pagedMembers = allMembers.subList(start, end);
        return new PageImpl<>(pagedMembers, pageable, allMembers.size());
    }

}