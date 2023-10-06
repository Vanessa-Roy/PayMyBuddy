CREATE TABLE connections (
    user1 VARCHAR(255) NOT NULL,
    user2 VARCHAR(255) NOT NULL
)  ENGINE=INNODB;
CREATE TABLE transactions (
    amount FLOAT(23) NOT NULL,
    date DATE NOT NULL,
    fee FLOAT(23) NOT NULL,
    transaction_id INTEGER NOT NULL AUTO_INCREMENT,
    description VARCHAR(255),
    receiver_user VARCHAR(255),
    sender_user VARCHAR(255),
    PRIMARY KEY (transaction_id)
)  ENGINE=INNODB;
CREATE TABLE users (
    balance FLOAT(23) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (email)
)  ENGINE=INNODB;
alter table connections add constraint FKg7j5sinwsj94hibjsbpypmmnt foreign key (user2) references users (email);
alter table connections add constraint FKqtybk7wwxuqogjsmo131jk8qa foreign key (user1) references users (email);
alter table transactions add constraint FK8aowqrxm3ga1xwc20ckpwkt5 foreign key (receiver_user) references users (email);
alter table transactions add constraint FK3kek2w7xoam6axb2aolaf85h6 foreign key (sender_user) references users (email);
