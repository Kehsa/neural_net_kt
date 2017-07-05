def image_print(ls, n):
    for (i, e) in enumerate(ls):
        if i % n == 0:
            print()
        if e > 0:
            print(1, end='')
        else:
            print(e, end='')


def num_print(n):
    print(n)
    for x in generator(n):
        print(x, end='')

def intListFromBytes(byte_arr):
    ls = []
    for px in byte_arr:
        ls.append(px)
    return ls


def norm(x):
    if x == 0:
        return 0
    return x / 255

def several(x1,x2,x3,x4):
    return int((x1 + x2 + x3 + x4) / 4)

def packer(img):
    res = []
    for y in range(14):
        for x in range(14):
            y2 = y*2
            x2 = x*2
            a = y2*28 + x2
            z = several(img[a], img[a+1], img[a+28], img[a+29])
            # yx yx+1 y+1x y+1x+1
            res.append(z)
    return res


def generator(x):
    for n in range(0, 10):
        if x == n: yield (n, 1)
        else: yield (n, 0)


b = 'big'
l = open('l', 'rb')
print(int.from_bytes(l.read(4), b, signed=False))
print(int.from_bytes(l.read(4), b, signed=False))
i = open('i', 'rb')
print(int.from_bytes(i.read(4), b, signed=False))
print(int.from_bytes(i.read(4), b, signed=False))
print(int.from_bytes(i.read(4), b, signed=False))
print(int.from_bytes(i.read(4), b, signed=False))
f = open('data.txt', 'w')

for n in range(0, 5000):
    lb = int.from_bytes(l.read(1), b, signed=False)
    img = i.read(784)
    img = packer(img)
    #image_print(img, 14)
    #num_print(lb)
    w = ''
    for px in img:
        w += str(norm(px)) + ','
    for (pos, n) in generator(lb):
        w += str(n)
        if pos != 9:
            w += ','
    w += '\n'
    f.write(w)
