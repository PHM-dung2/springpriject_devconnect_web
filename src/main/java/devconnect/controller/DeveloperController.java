package devconnect.controller;

import devconnect.model.dto.ProjectDto;
import devconnect.model.dto.developer.DeveloperDto;
import devconnect.model.dto.developer.DeveloperPwdUpdateDto;
import devconnect.model.entity.DeveloperEntity;
import devconnect.model.entity.DratingEntity;
import devconnect.model.entity.ProjectEntity;
import devconnect.model.entity.ProjectJoinEntity;
import devconnect.service.DeveloperService;
import devconnect.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/developer")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DeveloperController {
    private final DeveloperService developerService;

//    {
//        "did" : "qwe123",
//                "dpwd" : "qwe123",
//                "dname" : "qweqwe",
//                "dphone" : "010-1111-1111",
//                "daddress" : "인천시 미추홀구 도화동",
//                "demail" : "qwe@naver.com"
//    }

    // 1. 개발자 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Boolean> signUp( @ModelAttribute DeveloperDto developerDto ){
        boolean result = developerService.signUp( developerDto );
        if( !result ){ return ResponseEntity.status( 400 ).body( false ); }
        return ResponseEntity.status( 201 ).body( true );
    } // f end

//    { "did" : "qwe123", "dpwd" : "1234" }

    // 2. 개발자 로그인
    @PostMapping("/login")
    public ResponseEntity< ApiResponse<String> > logIn( @RequestBody DeveloperDto developerDto ){
        ApiResponse<String> result = developerService.logIn( developerDto );
        if( result.getData() != null ){ return ResponseEntity.status( 200 ).body( result ); }
        else{ return ResponseEntity.status( 400 ).body( result ); }
    } // f end

    // 3. 내 정보 조회
    @GetMapping("/info")
    public ResponseEntity<DeveloperDto> info( @RequestHeader("Authorization") String token ){
        DeveloperDto developerDto = developerService.info( token );
        if( developerDto != null ){ return ResponseEntity.status( 200 ).body( developerDto ); }
        else{ return ResponseEntity.status( 403 ).build(); }
    } // f end

    // 4. 로그아웃
    @GetMapping("/logout")
    public ResponseEntity<DeveloperDto> logout( @RequestHeader("Authorization") String token ){
        developerService.logout( token );
        return ResponseEntity.status( 204 ).build();
    } // f end

    // 5. 개발자 정보 수정
    @PutMapping("/update")
    public ResponseEntity<Boolean> onUpdate( @RequestHeader("Authorization") String token,
                                             @ModelAttribute DeveloperDto developerDto ){
        int logInDno;
        try{ logInDno = developerService.info( token ).getDno();
        }catch( Exception e ){ return ResponseEntity.status( 401 ).body( false ); }

        boolean result = developerService.onUpdate( developerDto, logInDno );
        if( result ){ return ResponseEntity.status( 200 ).body( true ); }
        else{ return ResponseEntity.status( 400 ).body( false ); }
    } // f end

    // 5-1. 개발자 비밀번호 수정
    @PutMapping("/update/pwd")
    public ResponseEntity<ApiResponse<Boolean>> onUpdatePwd(@RequestHeader("Authorization") String token ,
                                                   @RequestBody DeveloperPwdUpdateDto developerPwdUpdateDto ){
        int logInDno;
        try{
            logInDno = developerService.info( token ).getDno();
            ApiResponse<Boolean> result = developerService.onUpdatePwd( developerPwdUpdateDto, logInDno );
            return result.isSuccess() ? ResponseEntity.ok( result ) : ResponseEntity.badRequest().body( result );
        }catch( Exception e ){
            return ResponseEntity.status( 401 ).body( new ApiResponse<>( false, "토큰 인증 실패", null ) );
        }
    } // f end

    // 6. 개발자 정보 삭제 // 상태 수정으로 변경
    @PutMapping("/delete")
    public boolean onDelete( @RequestHeader("Authorization") String token,
                             @RequestBody String dpwd ){
        boolean result = developerService.onDelete( token, dpwd );
        return result;
    } // f end

    // 민진 추가
    // 7. 개발자 전체 조회
    @GetMapping("/findall")
    public ResponseEntity<List<DeveloperDto>> findAll( @RequestHeader("Authorization") String token ){
        List<DeveloperDto> List = developerService.findAll( token );
        if( List != null ){
            return ResponseEntity.ok(List);
        }else{
            return ResponseEntity.noContent().build();
        } // if end
    } // f end

    // 8. 개발자 순위 조회
    @GetMapping("/ranking")
    public ResponseEntity< Page<DeveloperDto> > ranking(
            @RequestParam( defaultValue = "1") int page,
            @RequestParam( defaultValue = "5") int size,
            @RequestParam( required = false ) String keyword ){
        System.out.println("page = " + page + ", size = " + size + ", keyword = " + keyword);
        Page< DeveloperDto > result = developerService.ranking( page, size, keyword );
        return ResponseEntity.status( 200 ).body( result );

    } // f end

    // 9. 개발자 기술 스택, 기업 평가 기반 전체 프로젝트 정렬 알고리즘
    @GetMapping("/sortproject")
    public ResponseEntity< Page<ProjectDto> > sortProjectByDno(
            @RequestHeader("Authorization") String token,
            @RequestParam( defaultValue = "1" ) int page,
            @RequestParam( defaultValue = "5" ) int size ){
        int logInDno;
        try{ logInDno = developerService.info( token ).getDno();
        }catch( Exception e ){ return ResponseEntity.status( 401 ).body( null ); }

        Page<ProjectDto> result = developerService.sortProjectByDno( logInDno, page, size );
        if( result != null ){ return ResponseEntity.status( 200 ).body( result ); }
        else{ return ResponseEntity.status( 403 ).build(); }
    } // f end

}

