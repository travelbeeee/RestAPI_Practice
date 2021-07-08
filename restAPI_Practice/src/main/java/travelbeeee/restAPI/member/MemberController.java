package travelbeeee.restAPI.member;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }

    @GetMapping("/member/{id}")
    public Member findMember(@PathVariable("id") Long id) {
        Optional<Member> findMember = memberRepository.findById(id);
        return findMember.orElse(null);
    }

    @GetMapping("/member")
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @PostMapping("/member")
    public Member uploadMember(@ModelAttribute Member member) {
        memberRepository.save(member);
        return member;
    }

    @DeleteMapping("/member/{id}")
    public Member deleteMember(@PathVariable("id") Long id) {
        Optional<Member> findMember = memberRepository.findById(id);
        if (!findMember.isEmpty()) {
            memberRepository.delete(findMember.get());
        }
        return null;
    }
}
