insert into warehouse values (null, 'Cajamar');
insert into warehouse values (null, 'Qatar');
insert into warehouse values (null, 'Uberlândia');

insert into manager values (null, 'pedro@example.com', 'Pedro', 'Thome');
insert into manager values (null, 'thiago@example.com', 'Thiago', 'Sordi');
insert into manager values (null, 'brunad@example.com', 'Bruna', 'Donadel');
insert into manager values (null, 'brunav@example.com', 'Bruna', 'Vottri');
insert into manager values (null, 'felipe@example.com', 'Felipe', 'Ticiane');
insert into manager values (null, 'viviane@example.com', 'Viviane', 'de Freitas');

insert into section values (null, 'FROZEN', 0, 500, 1, 1);
insert into section values (null, 'FRESH', 0, 500, 1, 1);
insert into section values (null, 'CHILLED', 0, 500, 1, 1);

insert into section values (null, 'FROZEN', 0, 20, 2, 2);
insert into section values (null, 'FRESH', 0, 5, 2, 2);
insert into section values (null, 'CHILLED', 5, 10, 3, 2);

insert into section values (null, 'FROZEN', 0, 10, 3, 3);
insert into section values (null, 'FRESH', 0, 5, 4, 3);
insert into section values (null, 'CHILLED', 0, 5, 4, 3);

insert into section values (null, 'FROZEN', 0, 10, 5, 3);
insert into section values (null, 'FRESH', 0, 10, 5, 3);
insert into section values (null, 'CHILLED', 0, 10, 5, 2);

insert into section values (null, 'FROZEN', 0, 5, 6, 2);
insert into section values (null, 'FRESH', 0, 5, 6, 1);
insert into section values (null, 'CHILLED', 0, 5, 6, 1);

insert into seller values (null, 'maria@example.com', 'Maria', 'maria');
insert into seller values (null, 'joao@example.com', 'João', 'joao');
insert into seller values (null, 'jose@example.com', 'José', 'jose');
insert into seller values (null, 'fulano@example.com', 'Fulano', 'fulano');
insert into seller values (null, 'beltrano@example.com', 'Beltrano', 'beltrano');
insert into seller values (null, 'ciclano@example.com', 'Ciclano', 'ciclano');

insert into product values (null, 'Danone', 'CHILLED', 'Iogurte', 1);
insert into product values (null, 'Sadia', 'CHILLED', 'Queijo', 2);
insert into product values (null, 'Seara', 'CHILLED', 'Salsicha', 2);
insert into product values (null, 'Vigor', 'CHILLED', 'Queijo', 3);
insert into product values (null, 'Catupiry', 'CHILLED', 'Requeijão', 5);

insert into product values (null, 'Sadia', 'FROZEN', 'Frango',2);
insert into product values (null, 'Sadia', 'FROZEN', 'Pizza', 3);
insert into product values (null, 'Seara', 'FROZEN', 'Lasanha', 3);
insert into product values (null, 'Perdigão', 'FROZEN', 'Batata', 4);
insert into product values (null, 'Seara', 'FROZEN', 'Hambúrguer', 1);

insert into product values (null, 'Qualitá', 'FRESH', 'Alface', 3);
insert into product values (null, 'Cepi', 'FRESH', 'Melancia', 4);
insert into product values (null, 'Amorango', 'FRESH', 'Morango', 4);
insert into product values (null, 'Plump', 'FRESH', 'Banana', 5);
insert into product values (null, 'Qualitá', 'FRESH', 'Beterraba', 2);

insert into buyer values (null, 'carol');
insert into buyer values (null, 'flavia');
insert into buyer values (null, 'carlos');
insert into buyer values (null, 'roberto');
insert into buyer values (null, 'jessica');
insert into buyer values (null, 'emerson');