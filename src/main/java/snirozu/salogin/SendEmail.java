package snirozu.salogin;

import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

public class SendEmail {
    // not using new for obvious reasons
    public static void sendEmail(String to, String subject, String content) {
        Email email = EmailBuilder.startingBlank()
            .from(Salogin.instance.getConfig().getString("email.name"), Salogin.instance.getConfig().getString("email.from"))
            .to(to)
            .withSubject(subject)
            .withPlainText(content)
            .buildEmail();

        Mailer mailer = MailerBuilder
            .withSMTPServer(
                Salogin.instance.getConfig().getString("email.host"), 
                Salogin.instance.getConfig().getInt("email.port"), 
                Salogin.instance.getConfig().getString("email.user"), 
                Salogin.instance.getConfig().getString("email.password"))
            .withTransportStrategy(TransportStrategy.SMTP_TLS)
            .buildMailer();

        mailer.sendMail(email);
    }
}
