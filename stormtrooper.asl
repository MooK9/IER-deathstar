//Alaphelyzet egyhelyben marad
//Kap jelet hogy gond van el kezd keresgélni
//ha tüzet talál, eloltja
// jelez hogy el van oltva a t?z
//vissza megy a kezd? helyére vagy marad ahol van


start.
+start<- .print("buzi vagyok").

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

+!at(T) : at(T).
+tuz(source(firealarm)): true <- ?pos(T,X,Y);
           move_towards(X,Y);
           !at(T).
		   
+elolt(T): true <- .send(firealarm,tell,eloltva).


