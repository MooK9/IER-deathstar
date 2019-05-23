//Alaphelyzet egyhelyben marad
//Kap jelet hogy gond van el kezd keresgélni
//ha tüzet talál, eloltja
// jelez hogy el van oltva a t?z
//vissza megy a kezd? helyére vagy marad ahol van

/*
start.
+start<- .print("buzi vagyok").

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

+!at(T) : at(T).
+tuz(source(firealarm)): true <- ?pos(T,X,Y);
           move_towards(X,Y);
           !at(T).
		   
+elolt(T): true <- .send(firealarm,tell,eloltva).
*/

@pfire[atomic]
+fire(X,Y)[source(firealarm)]
  <- !pos(X,Y);
     do(extinguish);
     -fire(X,Y)[source(_)];
     .send(firealarm,tell,extinguished(X,Y)).

+!pos(X,Y) : pos(X,Y)
  <- .print("Arrived to ", X, ", ", Y).

+!pos(X,Y) : not pos(X,Y)
  <- .print("going to ", X, ", ", Y);
     !next_step(X,Y);
     !pos(X,Y).

+!next_step(X,Y)
   :  pos(AgX,AgY)
   <- jia.get_direction(AgX, AgY, X, Y, D);
      do(D).

+!next_step(X,Y) : not pos(_,_)
   <- !next_step(X,Y).

-!next_step(X,Y) : true
   <- .print("Failed next_step to ", X,"x",Y," fixing and trying again!");
      !next_step(X,Y).

-fire(_,_) : not fire(_,_) & base5(X,Y)
  <- .print("Going home!");
     !pos(X,Y).


