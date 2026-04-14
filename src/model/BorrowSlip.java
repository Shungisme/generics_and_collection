package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BorrowSlip {
	private String slipId;
	private String readerId;
	private LocalDate borrowDate;
	private LocalDate expectedReturnDate;
	private LocalDate actualReturnDate;
	private List<String> isbnList;

	public BorrowSlip(
			String slipId,
			String readerId,
			LocalDate borrowDate,
			LocalDate expectedReturnDate,
			LocalDate actualReturnDate,
			List<String> isbnList) {
		this.slipId = slipId;
		this.readerId = readerId;
		this.borrowDate = borrowDate;
		this.expectedReturnDate = expectedReturnDate;
		this.actualReturnDate = actualReturnDate;
		this.isbnList = isbnList != null ? new ArrayList<>(isbnList) : new ArrayList<>();
	}

	public String getSlipId() {
		return slipId;
	}

	public void setSlipId(String slipId) {
		this.slipId = slipId;
	}

	public String getReaderId() {
		return readerId;
	}

	public void setReaderId(String readerId) {
		this.readerId = readerId;
	}

	public LocalDate getBorrowDate() {
		return borrowDate;
	}

	public void setBorrowDate(LocalDate borrowDate) {
		this.borrowDate = borrowDate;
	}

	public LocalDate getExpectedReturnDate() {
		return expectedReturnDate;
	}

	public void setExpectedReturnDate(LocalDate expectedReturnDate) {
		this.expectedReturnDate = expectedReturnDate;
	}

	public LocalDate getActualReturnDate() {
		return actualReturnDate;
	}

	public void setActualReturnDate(LocalDate actualReturnDate) {
		this.actualReturnDate = actualReturnDate;
	}

	public List<String> getIsbnList() {
		return isbnList;
	}

	public void setIsbnList(List<String> isbnList) {
		this.isbnList = isbnList != null ? new ArrayList<>(isbnList) : new ArrayList<>();
	}

	@Override
	public String toString() {
		return "BorrowSlip{" +
				"slipId='" + slipId + '\'' +
				", readerId='" + readerId + '\'' +
				", borrowDate=" + borrowDate +
				", expectedReturnDate=" + expectedReturnDate +
				", actualReturnDate=" + actualReturnDate +
				", isbnList=" + isbnList +
				'}';
	}
}
