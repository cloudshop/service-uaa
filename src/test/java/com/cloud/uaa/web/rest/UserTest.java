package com.cloud.uaa.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import com.cloud.uaa.domain.User;
import com.cloud.uaa.service.dto.UserAnnexDTO;
import com.cloud.uaa.service.dto.UserDTO;
import com.cloud.uaa.service.dto.WalletDTO;

import io.undertow.util.BadRequestException;

public class UserTest {

	@Test
	public void fun() throws Exception {
		File file = new File("C:\\development\\user.xlsx");
		FileInputStream fileInputStream = new FileInputStream(file);
		//HSSFWorkbook wb = new HSSFWorkbook(fileInputStream);
		XSSFWorkbook xb = new XSSFWorkbook(fileInputStream);
		//HSSFSheet sheet = wb.getSheetAt(0);
		XSSFSheet sheet = xb.getSheetAt(0);
		for (Row row : sheet) {
			int i = row.getRowNum();
			if (row.getCell(1) != null) {
				row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(2) != null) {
				row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(3) != null) {
				row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(4) != null) {
				row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
			}
			System.out.print(row.getCell(1)+"\t");//名称
			System.out.print(row.getCell(2).getStringCellValue()+"\t");//账号
			System.out.print(row.getCell(3)+"\t");//推荐人名称
			System.out.print(row.getCell(4)+"\t");//推荐人账号
			System.out.print(row.getCell(5)+"\t");//等级
			System.out.print(row.getCell(6)+"\t");//余额
			System.out.print(row.getCell(7)+"\t");//贡融卷
			System.out.print(row.getCell(8)+"\t\r");//贡融积分
			System.out.println("------------------------------");
			if (i == 10) {
				return;
			}
		}
	}
	

    /**
     * 导入用户数据
     * @author 逍遥子
     * @email 756898059@qq.com
     * @date 2018年5月2日
     * @version 1.0
     * @throws Exception
     */
    public void fun1() throws Exception {
    	Instant now = Instant.now();
		File file = new File("C:\\development\\user.xlsx");
		FileInputStream fileInputStream = new FileInputStream(file);
		XSSFWorkbook xb = new XSSFWorkbook(fileInputStream);
		XSSFSheet sheet = xb.getSheetAt(0);
		for (Row row : sheet) {
			int i = row.getRowNum();
			if (row.getCell(1) != null) {
				row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(2) != null) {
				row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(3) != null) {
				row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
			}
			if (row.getCell(5) != null) {
				row.getCell(5).setCellType(Cell.CELL_TYPE_STRING);
			}
			UserDTO userDTO = new UserDTO();
	    	userDTO.setLogin(row.getCell(2).getStringCellValue()); 
			//User user = userService.registerAppUser(userDTO, "123456");
			
//			UserAnnexDTO userAnnexDTO = new UserAnnexDTO();
//			userAnnexDTO.setId(user.getId());
//			userAnnexDTO.setCreatedTime(now);
//			userAnnexDTO.setUpdatedTime(now);
//			userAnnexDTO.setPhone(user.getLogin());
//			userAnnexDTO.setNickname(row.getCell(1).getStringCellValue());
//			switch (row.getCell(5).getStringCellValue()) {
//			case "普通会员":
//				userAnnexDTO.setType(1);
//				break;
//			case "VIP会员":
//				userAnnexDTO.setType(2);
//				break;
//			case "普通商户":
//				userAnnexDTO.setType(3);
//				break;
//			case "增值商户":
//				userAnnexDTO.setType(4);
//				break;
//			case "服务商":
//				userAnnexDTO.setType(5);
//				break;
//			default:
//				throw new BadRequestException("null--");
//			}
//			if (row.getCell(4) != null) {
//				row.getCell(4).setCellType(Cell.CELL_TYPE_STRING);
//				Optional<User> optional = userService.getUserWithAuthoritiesByLogin(row.getCell(4).getStringCellValue());
//				if (optional.isPresent()) {
//					userAnnexDTO.setInviterId(optional.get().getId());
//				}
//			}
//			userClient.createUserAnnex(userAnnexDTO);
//			
//			WalletDTO walletDTO = new WalletDTO();
//			walletDTO.setUserid(user.getId());
//			walletDTO.setCreateTime(now);
//			walletDTO.setVersion(0);
//			walletDTO.setUpdatedTime(now);
//			walletDTO.setBalance(new BigDecimal(row.getCell(6).getNumericCellValue()));
//			walletDTO.setTicket(new BigDecimal(row.getCell(7).getNumericCellValue()));
//			walletDTO.setIntegral(new BigDecimal(row.getCell(8).getNumericCellValue()));
//			walletService.createdWallet(walletDTO);
//			
			
//			System.out.print(row.getCell(1)+"\t");//名称
//			System.out.print(row.getCell(2).getStringCellValue()+"\t");//账号
//			System.out.print(row.getCell(3)+"\t");//推荐人名称
//			System.out.print(row.getCell(4)+"\t");//推荐人账号
//			System.out.print(row.getCell(5)+"\t");//等级
//			System.out.print(row.getCell(6)+"\t");//余额
//			System.out.print(row.getCell(7)+"\t");//贡融卷
//			System.out.print(row.getCell(8)+"\t\r");//贡融积分
//			System.out.println("------------------------------");
		}
    }
    
}
