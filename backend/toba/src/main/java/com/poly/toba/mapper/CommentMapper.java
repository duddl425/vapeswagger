package com.poly.toba.mapper;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.poly.toba.model.CommentDTO;
import com.poly.toba.model.LikeDTO;
import com.poly.toba.model.NoticeDTO;
import com.poly.toba.model.RecommentDTO;

@Mapper
public interface CommentMapper {

	public List<CommentDTO> getCommentList(HashMap<String, Object> hMap) throws Exception;

	public int insertComment(CommentDTO cDTO) throws Exception;

	public int commentListTotalCount(CommentDTO cDTO) throws Exception;

	public int recommentListTotalCount(RecommentDTO recDTO) throws Exception;

	public List<RecommentDTO> getRecommentList(HashMap<String, Object> hMap) throws Exception;

	public int insertRecomment(RecommentDTO recDTO) throws Exception;

	public int deleteComment(CommentDTO cDTO) throws Exception;

	public void deleteRecomment(CommentDTO cDTO) throws Exception;

	public int deleteRecommentSel(RecommentDTO reDTO) throws Exception;

	public LikeDTO likeCheck(LikeDTO likeDTO) throws Exception;

	public int likeUp(LikeDTO likeDTO) throws Exception;

	public int likeDown(LikeDTO likeDTO) throws Exception;

	public int likeCommentCount(LikeDTO likeDTO) throws Exception;

	public List<CommentDTO> pagingLikeCnt(HashMap<String, Object> hMap) throws Exception;

	public int commentCount(int noticeNo) throws Exception;
	
	public String getContent(CommentDTO cDTO) throws Exception;

	public String getRecomContent(RecommentDTO reDTO) throws Exception;
	
	public int commentUpd(CommentDTO cDTO) throws Exception;

	public int recommentUpd(RecommentDTO rDTO) throws Exception;

	public String getProfileImg(String commentWriter) throws Exception;

	public int commentTotalCount(HashMap<String, String> cMap) throws Exception;

	public List<CommentDTO> adminCommentList(HashMap<String, Object> hMap) throws Exception;

	public int commentEnDisable(HashMap<String, Object> hMap) throws Exception;
}
