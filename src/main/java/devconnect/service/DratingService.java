package devconnect.service;

import devconnect.model.dto.DratingDto;
import devconnect.model.entity.DeveloperEntity;
import devconnect.model.entity.DratingEntity;
import devconnect.model.entity.ProjectEntity;
import devconnect.model.repository.DeveloperRepository;
import devconnect.model.repository.DratingRepository;
import devconnect.model.repository.ProjectRepository;
import devconnect.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class DratingService {

    private final DratingRepository dratingRepository;
    private final ProjectRepository projectRepository;
    private final DeveloperRepository developerRepository;
    private final JwtUtil jwtUtil;

    // 개발자 평가 등록
    public boolean dratingWrite(DratingDto dratingDto , int loginCno ){
        System.out.println("DratingService.dratingWrite");
        // 입력받은 pno와 dno로 각각 엔티티 조회
        ProjectEntity projectEntity = projectRepository.findById( dratingDto.getPno())
                .orElseThrow( () -> new RuntimeException("프로젝트 번호가 존재하지 않습니다."));
        DeveloperEntity developerEntity = developerRepository.findById( dratingDto.getDno() )
                .orElseThrow( () -> new RuntimeException("개발자 번호가 존재하지 않습니다."));
        // pno와 dno에 값이 있을경우 true 없을시 false 반환
        if( projectEntity != null && developerEntity != null && projectEntity.getCompanyEntity().getCno() == loginCno ) {
            // Entity로 변환할때 조회한 엔티티를 포함하여 변환
            DratingEntity dratingEntity = dratingDto.toEntity(projectEntity, developerEntity);
            // Entity에 저장
            DratingEntity saveEntity = dratingRepository.save(dratingEntity);
            // 결과 반환
            if (saveEntity.getDrno() >= 1) {
                return true;
            } // if end
        } // if end
        return false;
    } // f end

    // 개발자 평가 전체 조회
    public Page<DratingDto> dratingList( String token , int page , int size , String keyword , int cno ){
        System.out.println("DratingService.dratingList");
        if( token != null ) {
            // Pageable 객체 생성
            Pageable pageable = PageRequest.of( page - 1, size, Sort.by(Sort.Direction.DESC , "drno"));
            // DratingEntity를 Repository를 이용하여 모든 정보 조회
            Page<DratingEntity> dratingListAll = dratingRepository.findBysearch( keyword , pageable , cno );
            // 필요한 필요한 정보들만 담은 Dto를 반환할 리스트 객체 생성
            Page<DratingDto> dratingList = dratingListAll.map(DratingEntity::toDto);
            return dratingList;
        } // if end
        // 리스트 객체 반환
        return null;
    } // f end

    // 개발자 평가 개별 조회
    public DratingDto dratingView( int drno , String token ){
        System.out.println("DratingService.dratingView");
        // 토큰 유무 검사
        if( token == null ) { return null; }
        // drno 를 기반으로 평가 조회
        Optional<DratingEntity> dOptional = dratingRepository.findById(drno);
        // 값이 있으면 true 없으면 false 로 조건문
        if (dOptional.isPresent()) {
            // 값을 Entity객체에 대입
            DratingEntity dratingEntity = dOptional.get();
            if( dratingEntity.getDrstate() != 1){ return null; }
            // dto로 변환
            DratingDto dratingDto = dratingEntity.toDto();
            return dratingDto;
        } // if end
        // 값이 없으면 null 반환
        return null;
    } // f end

    // 개발자 평가 수정
    public boolean dratingUpdate( DratingDto dratingDto , String token ){
        System.out.println("DratingService.dratingUpdate");
        // 토큰 유무 확인
        if( token == null ) { return false; }
        // dto에 입력한 drno로 해당 엔티티 조회
        Optional<DratingEntity> optional = dratingRepository.findById( dratingDto.getDrno() );
        if( optional.isPresent() ) {
            // 엔티티 타입으로 변환
            DratingEntity dratingEntity = optional.get();
            // 수정하는 cno와 등록했던 cno가 같은지 조건문
            String id = jwtUtil.valnoateToken(token);
            String code = jwtUtil.returnCode(id);
            if( id == null || code == null ){ return false; }
            if ( id.equals(dratingEntity.getProjectEntity().getCompanyEntity().getCid()) || code.equals("Admin") ) {
                // 수정
                dratingEntity.setDtitle(dratingDto.getDtitle());
                dratingEntity.setDcontent(dratingDto.getDcontent());
                dratingEntity.setDrscore(dratingDto.getDrscore());
                dratingEntity.setDrstate(0);
                // 값 수정후 true 반환
                return true;
            } // if end
        } // if end
        // 값이 없으면 false 반환
        return false;
    } // f end

    // 개발자 평가 삭제
    public boolean dratingDelete( int drno , String token ){
        System.out.println("DratingService.dratingDelete");
        // 토큰 유무 확인
        if( token == null ) { return false; }
        // 입력받은 drno로 엔티티 조회
        Optional< DratingEntity > optional = dratingRepository.findById( drno );
        // 조건문으로 엔티티 유무 확인
        if( optional.isPresent() ) {
            // 엔티티로 타입변환
            DratingEntity dratingEntity = optional.get();
            // 평가를 등록했던 기업과 삭제버튼을 누르는 기업의 cno가 같은지 확인 // 관리자 예외
            String id = jwtUtil.valnoateToken(token);
            String code = jwtUtil.returnCode(id);
            if( id == null || code == null ){ return false; }
            if ( id.equals(dratingEntity.getProjectEntity().getCompanyEntity().getCid()) || code.equals("Admin") ) {
                // 조회한 엔티티의 식별번호를 매개변수로 사용하여 삭제작업
                dratingRepository.deleteById(dratingEntity.getDrno());
                return true;
            } // if end
        } // if end
        return false;
    } // f end

} // c end