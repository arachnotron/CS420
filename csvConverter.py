
file_name = "matches10.csv"
ostring = ""
with open( file_name, 'r' ) as matches_csv:
    for line in matches_csv:
        if line[0] == "1":
            line = "TRUE" + line[1:]
        elif line[0] == "0":
            line = "FALSE" + line[1:]

        ostring += line

with open( file_name, 'w' ) as matches_csv:
    matches_csv.write( ostring )
