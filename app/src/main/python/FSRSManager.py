from fsrs import Scheduler, Card, Rating, ReviewLog
import os
import json
import ast

pathToScheduler = os.path.join(os.path.dirname(__file__),"scheduler.json")
pathToCards = os.path.join(os.path.dirname(__file__),"cards.json")

scheduler = Scheduler()
cards = []
def initialization():
    loadCards()
    loadScheduler()
def loadScheduler():
    global scheduler
    try:
        f = open(pathToScheduler, "r")
        scheduler = Scheduler.from_dict(f.read())
    except:
        scheduler = Scheduler()
        f = open(pathToScheduler, "w")
        f.write(str(scheduler.to_dict()))

def loadCards():
    global cards
    cards = []
    try:
        with open(pathToCards, 'r') as f:
            string_list = json.load(f)
            for card in string_list:
                cards.append(Card.from_dict(card))
    except:
        with open(pathToCards, 'w') as f:
            json.dump([card.to_dict() for card in cards], f)

def saveScheduler():
    f = open(pathToScheduler, "w")
    f.write(str(scheduler.to_dict()))

def saveCards():
    with open(pathToCards, 'w') as f:
        json.dump([card.to_dict() for card in cards], f)

def reviewCard(card, rating):
        current_card = Card.from_dict(ast.literal_eval(card))
        current_card, review_log = scheduler.review_card(current_card,rating_enumerator(rating))
        return current_card.to_dict()

def default_fsrs_value():
    loadScheduler()
    return Card().to_dict()

def get_due_date(cardStr):
    card = Card.from_dict(ast.literal_eval(cardStr))
    return card.due


def rating_enumerator(rating): 
   match rating:
      case "AGAIN":
         return Rating.Again
      case "GOOD":
         return Rating.Good
      case "HARD":
         return Rating.Hard
      case "EASY":
         return Rating.Easy
