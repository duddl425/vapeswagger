package com.poly.toba.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.toba.model.ImageBoardDTO;
import com.poly.toba.model.ImageBoardLikeDTO;
import com.poly.toba.model.PagingDTO;
import com.poly.toba.service.impl.ICommonService;
import com.poly.toba.service.impl.IImageBoardCommentService;
import com.poly.toba.service.impl.IImageBoardService;
import com.poly.toba.util.CmmUtil;
import com.poly.toba.util.MakeThumbnail;
import com.poly.toba.util.StringUtil;

@SpringBootApplication
@MapperScan(basePackages = "com.poly.toba")
@RestController
@RequestMapping("/imageBoards")
public class ImageBoardController {
	@Autowired
	 private IImageBoardService imageBoardService;
	 @Autowired
	 private IImageBoardCommentService imageBoardCommentService;
	 //이미지 게시판 5개 조회
	// 공지사항 최신 순 5개 리스트 가져오기
	@GetMapping("/mainImageBoardList")
	public ResponseEntity<HashMap<String, Object>> getMainImageBoardList() throws Exception {
		int imageBoardNo, imageBoardCommentCount;
		List<Integer> imageBoardCommentCountList = new ArrayList<>();
		HashMap<String, Object> hMap = new HashMap<>();
		List<ImageBoardDTO> ibList =imageBoardService.getMainImgBoardList();
		hMap.put("ibList", ibList);
		List<String> imgList = new ArrayList<>();
		List<String> thumbImgList = new ArrayList<>();
		for (int i = 0; i < ibList.size(); i++) {
			imageBoardNo = Integer.valueOf(ibList.get(i).getImageBoardNo());
			imageBoardCommentCount = imageBoardCommentService.commentCount(imageBoardNo);
			imageBoardCommentCountList.add(imageBoardCommentCount);
			imgList = StringUtil.getImgSrc(ibList.get(i).getImageBoardContent());
			thumbImgList.add(imgList.get(0));
		}
		hMap.put("imageBoardCommentCountList", imageBoardCommentCountList);
		hMap.put("thumbImgList", thumbImgList);
		
		return new ResponseEntity<HashMap<String, Object>>(hMap, HttpStatus.OK);
	}
	//페이징 첫조회
	 // 전체조회
	   @GetMapping("/list/{pageno}")
	   public ResponseEntity<HashMap<String, Object>> getImageBoardAllList(@PathVariable String pageno) throws Exception {
	    System.out.println("요청이 오냐");
		   HashMap<String, Object> resultMap = new HashMap<>();
	      // 페이징
	      PagingDTO paging = new PagingDTO();
	      int pagenum = Integer.parseInt(pageno);
	      int contentnum = 10;
	      int totalcount = 0;
	      totalcount = imageBoardService.imageBoardTotalCount();
	      paging.setTotalcount(totalcount);// 전체 게시글 지정
	      paging.setPagenum(pagenum - 1);// 현재페이지를 페이지 객체에 지정한다 -1 해야 쿼리에서 사용가능
	      paging.setContentnum(contentnum);// 한 페이지에 몇개 씩 게시글을 보여줄지 지정
	      paging.setCurrentblock(pagenum);// 현재 페이지 블록이 몇번인지 현재 페이지 번호를 통해서 지정함
	      paging.setLastblock(paging.getTotalcount());// 마지막 블록 번호를 전체 게시글 수를 통해 정함
	      paging.prevnext(pagenum); // 현재 페이지 번호로 화살표를 나타낼지 정함
	      paging.setStartPage(paging.getCurrentblock());// 시작페이지를 페이지 블록번호로 정함
	      paging.setEndPage(paging.getLastblock(), paging.getCurrentblock());// 마지막 페이지를 마지막 페이지 블록과 현재 페이지 블록번호로 정함
	      List<ImageBoardDTO> ibList = new ArrayList();
	      HashMap<String, Object> hMap = new HashMap<>();
	      
	      int i = paging.getPagenum() * 10;
	      int j = paging.getContentnum();
	      hMap.put("pagenum", i);
	      hMap.put("contentnum", j);
	      ibList = imageBoardService.getImageBoardList(hMap);
	      resultMap.put("ibList", ibList);
	      resultMap.put("paging", paging);
	      // 댓글, 좋아요 개수 가져오기
	      int imageBoardNo, imageBoardCommentCount, imageBoardLikeCount;
	      List<Integer> imageBoardCommentCountList = new ArrayList<>();
	      List<Integer> imageBoardLikeCountList = new ArrayList<>();
	      List<String> imgList = new ArrayList<>();
	  	  List<String> thumbImgList = new ArrayList<>();
	      for(int k=0; k<ibList.size(); k++) {
	    	 imageBoardNo = Integer.valueOf(ibList.get(k).getImageBoardNo());
	         System.out.println(imageBoardNo);
	         imageBoardCommentCount = imageBoardService.imageBoardCommentCountList(imageBoardNo);
	         imageBoardLikeCount =imageBoardService.imageBoardLikeCountList(imageBoardNo);
	         imageBoardCommentCountList.add(imageBoardCommentCount);
	         imageBoardLikeCountList.add(imageBoardLikeCount);
	 		 imgList = StringUtil.getImgSrc(ibList.get(k).getImageBoardContent());
			 thumbImgList.add(imgList.get(0));
	      }
	      resultMap.put("imageBoardCommentCountList", imageBoardCommentCountList);
	      resultMap.put("imageBoardLikeCountList", imageBoardLikeCountList);
	      resultMap.put("thumbImgList", thumbImgList);
	      imgList=null;
	      thumbImgList=null;
	      return new ResponseEntity<HashMap<String, Object>>(resultMap, HttpStatus.OK);
	   }
	   @GetMapping("/list/{pageno}/{searchWord}/{searchCategory}")
	   public ResponseEntity<HashMap<String, Object>> getNoticeList(@PathVariable String pageno,
	         @PathVariable String searchWord, @PathVariable String searchCategory) throws Exception {

	      HashMap<String, Object> resultMap = new HashMap<>();
	      // 페이징
	      PagingDTO paging = new PagingDTO();
	      int pagenum = Integer.parseInt(pageno);
	      int contentnum = 10;
	      int totalcount = 0;
	      int imageBoardNo, imageBoardCommentCount, imageBoardLikeCount;
	      List<Integer> imageBoardCommentCountList = new ArrayList<>();
	      List<Integer> imageBoardLikeCountList = new ArrayList<>();
	      if (CmmUtil.nvl(searchWord) != "") {
	         HashMap<String, Object> hMap = new HashMap<>();
	         hMap.put("searchWord", searchWord);
	         hMap.put("searchCategory", searchCategory);
	         totalcount = imageBoardService.imageBoardSearchTitleTotalCount(hMap);
	         paging.setTotalcount(totalcount);// 전체 게시글 지정
	         paging.setPagenum(pagenum - 1);// 현재페이지를 페이지 객체에 지정한다 -1 해야 쿼리에서 사용가능
	         paging.setContentnum(contentnum);// 한 페이지에 몇개 씩 게시글을 보여줄지 지정
	         paging.setCurrentblock(pagenum);// 현재 페이지 블록이 몇번인지 현재 페이지 번호를 통해서 지정함
	         paging.setLastblock(paging.getTotalcount());// 마지막 블록 번호를 전체 게시글 수를 통해 정함
	         paging.prevnext(pagenum); // 현재 페이지 번호로 화살표를 나타낼지 정함
	         paging.setStartPage(paging.getCurrentblock());// 시작페이지를 페이지 블록번호로 정함
	         paging.setEndPage(paging.getLastblock(), paging.getCurrentblock());// 마지막 페이지를 마지막 페이지 블록과 현재 페이지 블록번호로
	                                                            // 정함
	         List<ImageBoardDTO> ibList = new ArrayList();
	         int i = paging.getPagenum() * 10;
	         int j = paging.getContentnum();
	         hMap.put("pagenum", i);
	         hMap.put("contentnum", j);
	         ibList = imageBoardService.getImageBoardSearchTitleList(hMap);
	         resultMap.put("ibList", ibList);
	         resultMap.put("paging", paging);
	         List<String> imgList = new ArrayList<>();
		  	 List<String> thumbImgList = new ArrayList<>();
	         for (int k = 0; k < ibList.size(); k++) {
	        	 imageBoardNo = Integer.valueOf(ibList.get(k).getImageBoardNo());
	            hMap.put("imageBoardNo", imageBoardNo);
	        	 imageBoardCommentCount = imageBoardService.getImageBoardSearchCommentCount(hMap);
	        	 imageBoardLikeCount = imageBoardService.getImageBoardSearchLikeCount(hMap);
	        	 imageBoardCommentCountList.add(imageBoardCommentCount);
	        	 imageBoardLikeCountList.add(imageBoardLikeCount);
	        	 imgList = StringUtil.getImgSrc(ibList.get(k).getImageBoardContent());
				 thumbImgList.add(imgList.get(0));
	         }
	         resultMap.put("imageBoardCommentCountList", imageBoardCommentCountList);
	         resultMap.put("imageBoardLikeCountList", imageBoardLikeCountList);
	         resultMap.put("thumbImgList", thumbImgList);
		     imgList=null;
		     thumbImgList=null;
	      }
	      return new ResponseEntity<HashMap<String, Object>>(resultMap, HttpStatus.OK);

	   }
//	// 단일조회
	   @GetMapping("/detail/{imageBoardNo}")
	   public ResponseEntity<HashMap<String, Object>> getImageBoardDetail(@PathVariable String imageBoardNo) throws Exception {
	      ImageBoardDTO ibDTO = new ImageBoardDTO();
	      ImageBoardDTO prevDTO = new ImageBoardDTO();
	      ImageBoardDTO nextDTO = new ImageBoardDTO();
	      HashMap<String, Object> hMap = new HashMap<>();
	      
	      ibDTO.setImageBoardNo(imageBoardNo);
	      ibDTO = (ImageBoardDTO) imageBoardService.getImageBoardDetail(ibDTO);
	      
	      //이미지 리스트
	      List<String> imgList = new ArrayList<>();
	      imgList = StringUtil.getImgSrc(ibDTO.getImageBoardContent());
	      hMap.put("imgList", imgList);
	      // 조회수
	      int result = imageBoardService.imageBoardUpdateCount(ibDTO);
	      hMap.put("ibDTO",ibDTO);
	      hMap.put("imageBoardTotalCount", imageBoardService.imageBoardTotalCount());
	      // 좋아요 수 가져오기
	      ImageBoardLikeDTO blDTO = new ImageBoardLikeDTO();
	      blDTO.setImageBoardNo(imageBoardNo);
	      int imageBoardLikeCount = imageBoardService.imageBoardLikeTotalCount(blDTO);
	      hMap.put("imageBoardLikeCount", imageBoardLikeCount);
	      // 이전글, 다음글 댓글 개수
	      int prevCommentCount, nextCommentCount;
	      // 페이징
	      String prev, next;
	      if (ibDTO.getImageBoardPrev() == null && ibDTO.getImageBoardNext() != null) {
	         prev = "이전 글이 없습니다.";
	         prevDTO.setImageBoardNo("0");
	         prevDTO.setImageBoardTitle(prev);
	         nextDTO.setImageBoardNo(ibDTO.getImageBoardNext());
	         nextDTO = (ImageBoardDTO) imageBoardService.getImageBoardDetail(nextDTO);
	         nextCommentCount = imageBoardService.getImageBoardCommentCount(nextDTO);
	         hMap.put("prevDTO", prevDTO);
	         hMap.put("nextDTO", nextDTO);
	         hMap.put("nextCommentCount", nextCommentCount);
	         prevDTO.setPrevThumb("http://placehold.it/300x200");
	         nextDTO.setNextThumb(StringUtil.getImgSrc(nextDTO.getImageBoardContent()).get(0));
	      } else if (ibDTO.getImageBoardPrev() != null && ibDTO.getImageBoardNext() == null) {
	         prevDTO.setImageBoardNo(ibDTO.getImageBoardPrev());
	         prevDTO = (ImageBoardDTO) imageBoardService.getImageBoardDetail(prevDTO);
	         next = "다음 글이 없습니다.";
	         prevCommentCount = imageBoardService.getImageBoardCommentCount(prevDTO);
	         nextDTO.setImageBoardNo("0");
	         nextDTO.setImageBoardTitle(next);
	         hMap.put("prevDTO", prevDTO);
	         hMap.put("nextDTO", nextDTO);
	         hMap.put("prevCommentCount", prevCommentCount);
	         prevDTO.setPrevThumb(StringUtil.getImgSrc(prevDTO.getImageBoardContent()).get(0));
	         nextDTO.setNextThumb("http://placehold.it/300x200");
	      } else if (ibDTO.getImageBoardNext() != null && ibDTO.getImageBoardPrev() != null) {
	         prevDTO.setImageBoardNo(ibDTO.getImageBoardPrev());
	         prevDTO = (ImageBoardDTO) imageBoardService.getImageBoardDetail(prevDTO);
	         nextDTO.setImageBoardNo(ibDTO.getImageBoardNext());
	         nextDTO = (ImageBoardDTO) imageBoardService.getImageBoardDetail(nextDTO);
	         nextCommentCount = imageBoardService.getImageBoardCommentCount(nextDTO);
	         prevCommentCount = imageBoardService.getImageBoardCommentCount(prevDTO);
	         hMap.put("prevDTO", prevDTO);
	         hMap.put("nextDTO", nextDTO);
	         hMap.put("nextCommentCount", nextCommentCount);
	         hMap.put("prevCommentCount", prevCommentCount);
	         prevDTO.setPrevThumb(StringUtil.getImgSrc(prevDTO.getImageBoardContent()).get(0));
	         nextDTO.setNextThumb(StringUtil.getImgSrc(nextDTO.getImageBoardContent()).get(0));
	      } else {
	         prev = "이전 글이 없습니다.";
	         next = "다음 글이 없습니다.";
	         prevDTO.setImageBoardNo("0");
	         prevDTO.setImageBoardTitle(prev);
	         nextDTO.setImageBoardNo("0");
	         nextDTO.setImageBoardTitle(next);
	         hMap.put("prevDTO", prevDTO);
	         hMap.put("nextDTO", nextDTO);
	         prevDTO.setPrevThumb("http://placehold.it/300x200");
	         nextDTO.setNextThumb("http://placehold.it/300x200");
	      }
	      
	      return new ResponseEntity<HashMap<String, Object>>(hMap, HttpStatus.OK);
	   }
	   @PostMapping("/noticeSubmit")
      public ResponseEntity<Integer> regNotice(@RequestBody ImageBoardDTO nDTO) throws Exception {
         // 리눅스 기준으로 파일 경로를 작성 ( 루트 경로인 /으로 시작한다. )
         // 윈도우라면 workspace의 드라이브를 파악하여 JVM이 알아서 처리해준다.
         // 따라서 workspace가 C드라이브에 있다면 C드라이브에 upload 폴더를 생성해 놓아야 한다.
         String path = "/usr/local/tomcat/webapps/ROOT/imageUpload/imageBoard/";
         String newFileName = "";
         String thumbFileName ="";
         String extension = "";
         UUID uid = UUID.randomUUID();
         String now = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()); // 현재시간 나타내는 변수
         String year = now.substring(0,4);
        String month = now.substring(4,6);
        String day = now.substring(6,8);
        String hour = now.substring(8,10);
         String contentBase64 = nDTO.getImageBoardContent();
         int result = 0;
         List<String> oldSrc = new ArrayList<>();
         List<String> newSrc = new ArrayList<>();
         List<String> newFileNameList = new ArrayList<>();
         List<String> thumbFileNameList = new ArrayList<>();
         List<String> imgList = new ArrayList();
		 List<String> newThumbFilelist = new ArrayList<>();
		 List<String> extensionList = new ArrayList<>();
         
         result=imageBoardService.noticeReg(nDTO);
         imgList = StringUtil.getImgSrc(contentBase64);
         if(imgList.size()>0) {
            for (int i = 0; i < imgList.size(); i++) {
                String[] strings = imgList.get(i).toString().split(",");
                switch (strings[0]) {// 이미지 타입 체크
                case "data:image/jpeg;base64":
                   extension = "jpeg";
                   break;
                case "data:image/png;base64":
                   extension = "png";
                   break;
                case "data:image/gif;base64":
                   extension = "gif";
                   break;
                default:// 이미지 타입
                   extension = "jpg";
                   break;
                }
                // 변환 base64 string to binary data
                byte[] data = DatatypeConverter.parseBase64Binary(strings[1]);
                newFileName = path+year+"/"+month+"/"+day+"/"+hour+"/"+nDTO.getImageBoardNo()+"/"+uid + now + i + "." + extension;
                thumbFileName = uid + now + i;
                File filePath = new File(path+year+"/"+month+"/"+day+"/"+hour+"/"+nDTO.getImageBoardNo()+"/");
                if (!filePath.isDirectory()) {
                   filePath.mkdirs();
                }
                File file = new File(newFileName);
                try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                   outputStream.write(data);
                } catch (IOException e) {
                   e.printStackTrace();
                }
                //이미지 이동
                oldSrc.add(imgList.get(i).toString());
                newSrc.add("http://15.164.160.236:8080/imageUpload/imageBoard/"+year+"/"+month+"/"+day+"/"+hour+"/"+nDTO.getImageBoardNo()+"/thumbs/"+"THUMB_" + uid + now + i + "." + extension);
                newFileNameList.add(newFileName);
				thumbFileNameList.add(thumbFileName);
				extensionList.add(extension);
             }
            String replaceContent = StringUtil.getImgSrcReplace(contentBase64, oldSrc, newSrc);
            nDTO.setImageBoardContent(replaceContent);
            for(int k=0; k<thumbFileNameList.size();k++) {
               String thumbnailPath=MakeThumbnail.makeThumbnail(path,newFileNameList.get(k), thumbFileNameList.get(k),extensionList.get(k),year,month,day,hour,nDTO.getImageBoardNo());
               newThumbFilelist.add(thumbnailPath);
            }
           
            int result2 = imageBoardService.updateThumbnail(nDTO);
            //원본 파일 삭제
            if(result2 == 1) {
               for (String imgPath : newFileNameList) {
                  File file2 = new File(imgPath);
                  file2.delete();
               } 
            }
         }else if(imgList.size()== 0) {
            int result2 = imageBoardService.updateThumbnail(nDTO);
         }
         newFileNameList=null;
         thumbFileNameList=null;
         oldSrc=null;
         newSrc=null;
         imgList=null;
         if (result == 1) {
            return new ResponseEntity<Integer>(result, HttpStatus.OK);
         } else {
            return new ResponseEntity<Integer>(result, HttpStatus.OK);
         }
      }
}
