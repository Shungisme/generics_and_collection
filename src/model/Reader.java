package model;

import java.time.LocalDate;

public class Reader {
	private String readerId;
	private String fullName;
	private String idCard;
	private LocalDate dateOfBirth;
	private String gender;
	private String email;
	private String address;
	private LocalDate cardCreatedDate;
	private LocalDate cardExpiredDate;

	public Reader(
			String readerId,
			String fullName,
			String idCard,
			LocalDate dateOfBirth,
			String gender,
			String email,
			String address,
			LocalDate cardCreatedDate) {
		this.readerId = readerId;
		this.fullName = fullName;
		this.idCard = idCard;
		this.dateOfBirth = dateOfBirth;
		this.gender = gender;
		this.email = email;
		this.address = address;
		setCardCreatedDate(cardCreatedDate);
	}

	public String getReaderId() {
		return readerId;
	}

	public void setReaderId(String readerId) {
		this.readerId = readerId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LocalDate getCardCreatedDate() {
		return cardCreatedDate;
	}

	public void setCardCreatedDate(LocalDate cardCreatedDate) {
		this.cardCreatedDate = cardCreatedDate;
		this.cardExpiredDate = cardCreatedDate != null ? cardCreatedDate.plusMonths(48) : null;
	}

	public LocalDate getCardExpiredDate() {
		return cardExpiredDate;
	}

	public void setCardExpiredDate(LocalDate cardExpiredDate) {
		this.cardExpiredDate = cardExpiredDate;
	}

	@Override
	public String toString() {
		return "Reader{" +
				"readerId='" + readerId + '\'' +
				", fullName='" + fullName + '\'' +
				", idCard='" + idCard + '\'' +
				", dateOfBirth=" + dateOfBirth +
				", gender='" + gender + '\'' +
				", email='" + email + '\'' +
				", address='" + address + '\'' +
				", cardCreatedDate=" + cardCreatedDate +
				", cardExpiredDate=" + cardExpiredDate +
				'}';
	}
}
