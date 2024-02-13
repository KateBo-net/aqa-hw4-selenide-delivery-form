import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class DeliveryFormTest {
    //data
    String baseUrl = "http://localhost:9999/";
    String city = "Ростов-на-Дону";
    int availableDate = 3;
    String name = "Иванов Иван";
    String phone = "+79001122333";
    //msg
    String emptyInputField = "Поле обязательно для заполнения";
    String sendButtonText = "Забронировать";
    String notificationTitle = "Успешно!";
    String notificationContent = "Встреча успешно забронирована на ";
    String invalidCityMsg = "Доставка в выбранный город недоступна";
    String notADateMsg = "Неверно введена дата";
    String invalidDateMsg = "Заказ на выбранную дату невозможен";
    String invalidNameMsg = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";
    String invalidPhoneMsg = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";
    //selectors
    String citySelector = "[data-test-id=city] input";
    String cityInvalidSelector = "[data-test-id=city].input_invalid .input__sub";
    String dateSelector = "[data-test-id=date] input";
    String dateInvalidSelector = "[data-test-id=date] .input_invalid .input__sub";
    String nameSelector = "[data-test-id=name] input";
    String nameInvalidSelector = "[data-test-id=name].input_invalid .input__sub";
    String phoneSelector = "[data-test-id=phone] input";
    String phoneInvalidSelector = "[data-test-id=phone].input_invalid .input__sub";
    String agreementSelector = "[data-test-id=agreement] span.checkbox__box";
    String agreementInvalidSelector = "[data-test-id=agreement].input_invalid";
    String buttonSelector = "button .button__text";
    String notificationTitleSelector = "[data-test-id=notification] .notification__title";
    String notificationContentSelector = "[data-test-id=notification] .notification__content";
    String cityPopupSelector = ".input__popup .menu";

    public void stepsToFillForm(String city, int dateAmount, String name, String phone, Boolean agreementClick) {
        $(citySelector).setValue(city);
        clearField(dateSelector);
        $(dateSelector).setValue(calculateDate(dateAmount));
        $(nameSelector).setValue(name);
        $(phoneSelector).setValue(phone);
        if (agreementClick) {
            $(agreementSelector).click();
        }
        $(buttonSelector).shouldHave(text(sendButtonText)).click();
    }

    public void stepsToValidateDate(String city, String date, String name, String phone) {
        $(citySelector).setValue(city);
        clearField(dateSelector);
        $(dateSelector).setValue(date);
        $(nameSelector).setValue(name);
        $(phoneSelector).setValue(phone);
        $(agreementSelector).click();
        $(buttonSelector).shouldHave(text(sendButtonText)).click();
    }

    String calculateDate(int amount) {
        Calendar calendar = new GregorianCalendar();
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        calendar.add(Calendar.DAY_OF_MONTH, amount);
        String formattedDate = df.format(calendar.getTime());
        return formattedDate;
    }

    void clearField(String selector) {
        $(selector).sendKeys(Keys.CONTROL + "A");
        $(selector).sendKeys(Keys.BACK_SPACE);
    }

    @BeforeEach
    void setUpAll() {
        open(baseUrl);
    }

    @Test
    void shouldSendApplication() {
        stepsToFillForm(city, availableDate, name, phone, true);
        $(notificationTitleSelector)
                .should(visible, Duration.ofSeconds(15)).shouldHave(text(notificationTitle));
        $(notificationContentSelector).shouldHave(visible, text(notificationContent + calculateDate(availableDate)));

    }

    @Test
    void shouldNotPassValidationEmptyFields() {
        clearField(dateSelector);
        $(buttonSelector).shouldHave(text(sendButtonText)).click();
        $(cityInvalidSelector).shouldHave(text(emptyInputField));
        $(dateInvalidSelector).shouldNot(exist);
        $(nameInvalidSelector).shouldNot(exist);
        $(phoneInvalidSelector).shouldNot(exist);
        $(agreementInvalidSelector).shouldNot(exist);
        $(notificationTitleSelector).should(hidden);
    }

    @Test
    void shouldNoPassValidationWithoutAgreement() {
        stepsToFillForm(city, availableDate, name, phone, false);
        $(agreementInvalidSelector).should(visible);
        $(notificationTitleSelector).should(hidden);
    }

    @Test
    void shouldValidateFieldCityEmpty() {
        stepsToFillForm("", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(emptyInputField));
    }

    @Test
    void shouldValidateFieldCityLowerCase() {
        stepsToFillForm(city.toLowerCase(), availableDate, name, phone, true);
        $(cityInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldCityUpperCase() {
        stepsToFillForm(city.toUpperCase(), availableDate, name, phone, true);
        $(cityInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldCityNotACentre() {
        //not an administrative center
        stepsToFillForm("Химки", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(invalidCityMsg));
    }

    @Test
    void shouldValidateFieldCityMissedAHyphen() {
        stepsToFillForm("Ростов на дону", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(invalidCityMsg));
    }

    @Test
    void shouldValidateFieldCityLatin() {
        stepsToFillForm("Moscow", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(invalidCityMsg));
    }

    @Test
    void shouldValidateFieldCityNumbers() {
        stepsToFillForm("Челябинск74", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(invalidCityMsg));
    }

    @Test
    void shouldValidateFieldCitySpecialChar() {
        stepsToFillForm("?'%", availableDate, name, phone, true);
        $(cityInvalidSelector).shouldHave(text(invalidCityMsg));
    }

    @Test
    void shouldValidateFieldDateEmpty() {
        stepsToValidateDate(city, "", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateBeforeAvailableDate() {
        stepsToFillForm(city, availableDate - 1, name, phone, true);
        $(dateInvalidSelector).shouldHave(text(invalidDateMsg));
    }

    @Test
    void shouldValidateFieldDateAfterAvailableDate() {
        stepsToFillForm(city, availableDate + 1, name, phone, true);
        $(dateInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldDateInvalidDay() {
        stepsToValidateDate(city, "32.08.2024", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateInvalidMonth() {
        stepsToValidateDate(city, "01.13.2024", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateInvalidYear() {
        stepsToValidateDate(city, "01.02.24", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateOnlyNumbers() {
        stepsToValidateDate(city, "535", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateLatinLetters() {
        stepsToValidateDate(city, "10 april 2024", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateCyrillicLetters() {
        stepsToValidateDate(city, "10 авг 2024", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldDateSpecialChar() {
        stepsToValidateDate(city, "%^@", name, phone);
        $(dateInvalidSelector).shouldHave(text(notADateMsg));
    }

    @Test
    void shouldValidateFieldNameEmpty() {
        stepsToFillForm(city, availableDate, "", phone, true);
        $(nameInvalidSelector).shouldHave(text(emptyInputField));
    }

    @Test
    void shouldValidateFieldNameWithHyphen() {
        stepsToFillForm(city, availableDate, "Петрова Эмилия-Анна", phone, true);
        $(nameInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldNameLowerCase() {
        stepsToFillForm(city, availableDate, name.toLowerCase(), phone, true);
        $(nameInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldNameUpperCase() {
        stepsToFillForm(city, availableDate, name.toUpperCase(), phone, true);
        $(nameInvalidSelector).shouldNot(exist);
    }

    @Test
    void shouldValidateFieldNameLatin() {
        stepsToFillForm(city, availableDate, "Ivanov Ivan", phone, true);
        $(nameInvalidSelector).shouldHave(text(invalidNameMsg));
    }

    @Test
    void shouldValidateFieldNameNumbers() {
        stepsToFillForm(city, availableDate, "Иванов Иван78", phone, true);
        $(nameInvalidSelector).shouldHave(text(invalidNameMsg));
    }

    @Test
    void shouldValidateFieldNameSpecialChar() {
        stepsToFillForm(city, availableDate, "$%^&*", phone, true);
        $(nameInvalidSelector).shouldHave(text(invalidNameMsg));
    }

    @Test
    void shouldValidateFieldPhoneEmpty() {
        stepsToFillForm(city, availableDate, name, "", true);
        $(phoneInvalidSelector).shouldHave(text(emptyInputField));
    }

    @Test
    void shouldValidateFieldPhoneLess11Digits() {
        stepsToFillForm(city, availableDate, name, "+7900112233", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhoneMore11Digits() {
        stepsToFillForm(city, availableDate, name, phone + "1", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhoneWithoutPlus() {
        stepsToFillForm(city, availableDate, name, "79001122333", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhonePlusAtTheEnd() {
        stepsToFillForm(city, availableDate, name, "79001122333+", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhoneLatin() {
        stepsToFillForm(city, availableDate, name, "phone79991122333", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhoneCyrillic() {
        stepsToFillForm(city, availableDate, name, "тел79991122333", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldValidateFieldPhoneSpecialChar() {
        stepsToFillForm(city, availableDate, name, "-%^79991122333", true);
        $(phoneInvalidSelector).shouldHave(text(invalidPhoneMsg));
    }

    @Test
    void shouldSelectCityFromDropDownList() {
        $(citySelector).setValue("ро");
        $(cityPopupSelector).should(visible);
        $$(".input__popup .menu .menu-item").find(text(city)).click();

        String actual = $(citySelector).getValue();
        String expected = city;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldNotDisplayedDropDownList() {
        $(citySelector).setValue("к");
        $(cityPopupSelector).should(hidden);
    }

    @Test
    void shouldSelectDateFromDatePickerNextMonth() {
        Calendar calendar = new GregorianCalendar();
        DateFormat day = new SimpleDateFormat("d");
        DateFormat month = new SimpleDateFormat("LLLL yyyy");
        calendar.add(Calendar.DAY_OF_MONTH, 20);
        String formattedDay = day.format(calendar.getTime());
        String formattedMonth = month.format(calendar.getTime());

        $(".input__icon").click();
        $(".popup .calendar").should(visible);
        String nameCalendar = $(".calendar__name")
                .should(visible)
                .text().toLowerCase();
        if (nameCalendar.equals(formattedMonth)) {
            $$(".calendar__day").find(text(formattedDay)).click();
        } else {
            $("[data-step='1']").click();
            $$(".calendar__day").find(text(formattedDay)).click();
        }

        String actual = $(dateSelector).getValue();
        String expected = calculateDate(20);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void shouldSelectDateFromDatePickerNextWeek() {
        Calendar calendar = new GregorianCalendar();
        DateFormat day = new SimpleDateFormat("d");
        DateFormat month = new SimpleDateFormat("LLLL yyyy");
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        String formattedDay = day.format(calendar.getTime());
        String formattedMonth = month.format(calendar.getTime());
        $(".input__icon").click();
        $(".popup .calendar").should(visible);
        String nameCalendar = $(".calendar__name")
                .should(visible)
                .text().toLowerCase();
        if (nameCalendar.equals(formattedMonth)) {
            $$(".calendar__day").find(text(formattedDay)).click();
        } else {
            $("[data-step='1']").click();
            $$(".calendar__day").find(text(formattedDay)).click();
        }
        String actual = $(dateSelector).getValue();
        String expected = calculateDate(7);
        Assertions.assertEquals(expected, actual);
    }
}
