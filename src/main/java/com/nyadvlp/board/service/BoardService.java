package com.nyadvlp.board.service;

import com.nyadvlp.board.domain.entity.BoardEntity;
import com.nyadvlp.board.domain.repository.BoardRepository;
import com.nyadvlp.board.dto.BoardDto;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private static final int BLOCK_PAGE_NUM_COUNT = 5;
    private static final int PAGE_POST_COUNT = 4;

    @Transactional
    public long savePost(BoardDto boardDto) {
        return boardRepository.save(boardDto.toEntity()).getId();
        // save는 JpaRepository에 정의된 메서드로, Insert와 Update를 담당하며 매개변수는 Entity임
    }

    @Transactional
    public List<BoardDto> getBoardList(Integer pageNum) {
        // pageNum이 전달되면 해당 페이지로, 전달되지 않으면 default값인 1로 넘어옴 (컨트롤러의 defaultValue = "1" 부분)

        Page<BoardEntity> page = boardRepository
                .findAll(PageRequest.of
                        (pageNum - 1, PAGE_POST_COUNT, Sort.by(Sort.Direction.DESC, "createdDate")));
                        // 첫번째 인자 : page - 몇 페이지?
                        // 두번째 인자 : size(offset) - 몇 개 가져올 것인가 (5개)
                        // 세번째 인자 : sort - 정렬방식
        // Pageable 인터페이스를 구현한 클래스를 전달하면 페이징을 할 수 있음

        List<BoardEntity> boardEntities = page.getContent();
        List<BoardDto> boardDtoList = new ArrayList<>();
        
        // Page로 Entity 꺼내고 -> getContent로 Entity 또 꺼내고 -> DtoList에 담음

        for (BoardEntity boardEntity : boardEntities) {
            boardDtoList.add(this.convertEntityToDto(boardEntity));
        }

        return boardDtoList;
    }

    @Transactional
    public Long getBoardCount() {
        return boardRepository.count(); // 전체 게시글 수
    }

    // 노출할 페이지 번호 블럭
    public Integer[] getPageList(Integer curPageNum) {
        Integer[] pageList = new Integer[BLOCK_PAGE_NUM_COUNT];

        // 총 게시글 갯수
        Double postsTotalCount = Double.valueOf(this.getBoardCount());

        // 총 게시글 기준으로 계산한 마지막 페이지 번호 계산 (올림으로 계산)
        // 글이 3개면 1페이지, 9개면 3페이지, 100개면 20페이지
        Integer totalLastPageNum = (int)(Math.ceil((postsTotalCount/PAGE_POST_COUNT)));

        // 블럭 번호 세팅
        Integer blockStartPageNum = (curPageNum <= BLOCK_PAGE_NUM_COUNT / 2)
                ? 1
                : curPageNum - BLOCK_PAGE_NUM_COUNT / 2;

        // 현재 페이지를 기준으로 블럭의 마지막 페이지 번호 계산
        Integer blockLastPageNum =
                (totalLastPageNum > blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1 )
                        ? blockStartPageNum + BLOCK_PAGE_NUM_COUNT - 1
                        : totalLastPageNum;

        // 페이지 가운데 숫자 조정
        curPageNum = (curPageNum <= 3) ? 1 : curPageNum - 2;

        // 페이지 번호 할당
        for (int val = blockStartPageNum, idx = 0; val <= blockLastPageNum; val++, idx++) {
            pageList[idx] = val;
        }

        return pageList;
    }

    @Transactional
    public BoardDto getPost(Long id) {
        Optional<BoardEntity> boardEntityWrapper = boardRepository.findById(id);
        // 엔티티를 빼기 위해서는 get() 메소드를 사용해야 함
        BoardEntity boardEntity = boardEntityWrapper.get();

//        BoardDto boardDto = BoardDto.builder()
//                .id(boardEntity.getId())
//                .title(boardEntity.getTitle())
//                .content(boardEntity.getContent())
//                .writer(boardEntity.getWriter())
//                .createdDate(boardEntity.getCreatedDate())
//                .build();

        BoardDto boardDto = this.convertEntityToDto(boardEntity);

        return boardDto;
    }

    @Transactional
    public void deletePost(Long id) {
        boardRepository.deleteById(id);
    }


    public List<BoardDto> searchPosts(String keyword) {
        List<BoardEntity> boardEntities = boardRepository.findByTitleContaining(keyword);
        List<BoardDto> boardDtoList = new ArrayList<>();

        if (boardEntities.isEmpty()) return boardDtoList;

        for (BoardEntity boardEntity : boardEntities) {
            boardDtoList.add(this.convertEntityToDto(boardEntity));
        }
        return boardDtoList;
    }

    private BoardDto convertEntityToDto(BoardEntity boardEntity) {
        return BoardDto.builder()
                .id(boardEntity.getId())
                .title(boardEntity.getTitle())
                .content(boardEntity.getContent())
                .writer(boardEntity.getWriter())
                .createdDate(boardEntity.getCreatedDate())
                .modifiedDate(boardEntity.getModifiedDate())
                .build();
    }


}
