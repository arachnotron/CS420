
ostring = "Team1Win,Team1AvgChampionWR,Team1AvgPlayerChampionWR,Team1AvgRecentWR,Team1AvgChampionPickFrequency,Team1AvgRelativeRanking,Team2AvgChampionWR,Team2AvgPlayerChampionWR,Team2AvgRecentWR,Team2AvgChampionPickFrequency,Team2AvgRelativeRanking\n"
with open( "matches.csv" ) as matches_csv:
    for line in matches_csv:
        if line[0:4] == "Team":
            continue
        line = line.split(',')
        team1ChampAvg = (float(line[1]) + float(line[2]) + float(line[3]) + float(line[4]) + float(line[5]) )/5
        team1PCAvg = (float(line[6]) + float(line[7]) +float(line[8]) + float(line[9]) + float(line[10]) ) /5
        team1RecentAvg = ( float(line[11]) + float(line[12]) + float(line[13]) + float(line[14]) + float(line[15]) )/5
        team1ChampFreq = ( float(line[16]) + float(line[17]) + float(line[18]) + float(line[19]) + float(line[20]) )/5
        team1Ranking = ( int(line[21]) + int(line[22]) + int(line[23]) + int(line[24]) + int(line[25]) )/5
        
        team2ChampAvg = ( float(line[26]) + float(line[27]) + float(line[28]) + float(line[29]) + float(line[30]) )/5
        team2PCAvg = ( float(line[31]) + float(line[32]) + float(line[33]) + float(line[34]) + float(line[35]) )/5
        team2RecentAvg = ( float(line[36]) + float(line[37]) + float(line[38]) + float(line[39]) + float(line[40]) )/5
        team2ChampFreq = ( float(line[41]) + float(line[42]) + float(line[43]) + float(line[44]) + float(line[45]) )/5
        team2Ranking = ( int(line[46]) + int(line[47]) + int(line[48]) + int(line[49]) + int(line[50]) )/5

        ostring += line[0]+','+str(team1ChampAvg)+','+str(team1PCAvg)+','+str(team1RecentAvg)+','+str(team1ChampFreq)+','+str(team1Ranking)+','+str(team2ChampAvg)+','+str(team2PCAvg)+','+str(team2RecentAvg)+','+str(team2ChampFreq)+','+str(team2Ranking)+'\n'

with open( "matches_avg.csv", 'w' ) as out_csv:
    out_csv.write( ostring )
