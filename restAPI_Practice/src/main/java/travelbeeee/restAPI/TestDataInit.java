package travelbeeee.restAPI;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import travelbeeee.restAPI.member.Member;
import travelbeeee.restAPI.member.MemberRepository;

import javax.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class TestDataInit {

    private final MemberRepository memberRepository;
    /**
     * 테스트용 데이터 추가
     */
    @PostConstruct
    public void init() {
        memberRepository.save(new Member("member1", "email1"));
        memberRepository.save(new Member("member2", "email2"));
    }
}