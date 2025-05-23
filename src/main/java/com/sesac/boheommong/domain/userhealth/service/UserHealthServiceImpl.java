package com.sesac.boheommong.domain.userhealth.service;

import com.sesac.boheommong.domain.user.entity.User;
import com.sesac.boheommong.domain.user.repository.UserRepository;
import com.sesac.boheommong.domain.userhealth.dto.request.UserHealthRequestDto;
import com.sesac.boheommong.domain.userhealth.dto.response.UserHealthResponseDto;
import com.sesac.boheommong.domain.userhealth.entity.UserHealth;
import com.sesac.boheommong.domain.userhealth.enums.JobType;
import com.sesac.boheommong.domain.userhealth.repository.UserHealthRepository;
import com.sesac.boheommong.global.exception.BaseException;
import com.sesac.boheommong.global.exception.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHealthServiceImpl implements UserHealthService {

    private final UserHealthRepository userHealthRepository;
    private final UserRepository userRepository;

    /**
     * [건강정보 등록]
     */
    @Transactional
    @Override
    public UserHealthResponseDto createHealth(Long userId, UserHealthRequestDto request) {
        // 1) User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) 이미 존재하면 예외
        userHealthRepository.findByUser(user).ifPresent(h -> {
            throw BaseException.from(ErrorCode.USER_HEALTH_ALREADY_EXISTS);
        });

        // 3) jobType 변환 (기본적으로 'OFFICE','DELIVERY' 등 enum name과 매칭)
        JobType jobEnum = JobType.valueOf(request.jobType());

        String diseaseStr = convertListToCommaString(request.chronicDiseaseList());

        // 4) 엔티티 생성
        UserHealth newHealth = UserHealth.create(
                user,
                request.age(),
                request.gender(),
                request.height(),
                request.weight(),
                request.bloodPressureLevel(),
                request.bloodSugarLevel(),
                request.surgeryCount(),
                request.isSmoker(),
                request.isDrinker(),
                diseaseStr,
                jobEnum,
                request.hasChildren(),
                request.hasOwnHouse(),
                request.hasPet(),
                request.hasFamilyHistory()
        );

        // 5) 저장
        UserHealth saved = userHealthRepository.save(newHealth);

        // 6) DTO 변환
        return UserHealthResponseDto.fromEntity(saved);
    }

    // List -> 콤마 구분 문자열 변환
    private String convertListToCommaString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    /**
     * [건강정보 수정]
     */
    @Transactional
    @Override
    public UserHealthResponseDto updateHealth(Long userId, UserHealthRequestDto request) {
        // 1) User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) 기존 health 찾기
        UserHealth existing = userHealthRepository.findByUser(user)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_HEALTH_NOT_FOUND));

        // 3) jobType 변환
        JobType jobEnum = JobType.valueOf(request.jobType());

        String diseaseStr = convertListToCommaString(request.chronicDiseaseList());

        // 4) update
        existing.updateHealth(
                request.age(),
                request.gender(),
                request.height(),
                request.weight(),
                request.bloodPressureLevel(),
                request.bloodSugarLevel(),
                request.surgeryCount(),
                request.isSmoker(),
                request.isDrinker(),
                diseaseStr,
                jobEnum,
                request.hasChildren(),
                request.hasOwnHouse(),
                request.hasPet(),
                request.hasFamilyHistory()
        );

        // 5) 응답 DTO로 변환
        return UserHealthResponseDto.fromEntity(existing);
    }

    /**
     * [건강정보 조회]
     */
    @Transactional(readOnly = true)
    @Override
    public UserHealthResponseDto getMyHealth(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) UserHealth 조회
        UserHealth health = userHealthRepository.findByUser(user)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_HEALTH_NOT_FOUND));

        return UserHealthResponseDto.fromEntity(health);
    }

    @Transactional
    @Override
    public void deleteHealth(Long userId) {
        // 1) User 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_NOT_FOUND));

        // 2) UserHealth 조회
        UserHealth userHealth = userHealthRepository.findByUser(user)
                .orElseThrow(() -> BaseException.from(ErrorCode.USER_HEALTH_NOT_FOUND));

        // 3) 삭제 (JPA 영속성 컨텍스트에서 제거)
        userHealthRepository.delete(userHealth);
    }
}
