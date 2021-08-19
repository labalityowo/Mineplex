const fastify = require('fastify')({
  logger: true
});


//Accounts
fastify.register(require('fastify-mysql'), {
  promise: true,
  connectionString: 'mysql://root:root@localhost/account'
})

fastify.post('/PlayerAccount/GetAccountByUUID', {
  schema: {
    body: {
      type: 'string',
    }
  }
}, async (request, reply) => {
  console.log("GetAccountByUUID called")
  console.log(request)
});

fastify.post('/PlayerAccount/GetAccount', {
  schema: {
    body: {
      type: 'string',
    }
  }
}, async (request, reply) => {
  console.log("GetAccount called")
  console.log(request)
});

fastify.post('/PlayerAccount/Login', {
  schema: {
    body: {
      type: 'object',
      properties: {
        MacAddress: { type: 'string' },
        IpAddress: { type: 'string' },
        Name: { type: 'string' },
        Uuid: { type: 'string' },
      }
    }
  }
},
  async (request, reply) => {
    const connection = await fastify.mysql.getConnection()

    const loginToken = request.body;
    const accountResult = await connection.query(
      'SELECT * FROM accounts WHERE uuid=?', [loginToken.Uuid],
    );

    const account = accountResult[0][0];
    const accountRankResult = await connection.query(
      'SELECT * FROM accountranks WHERE accountId=?', [account.id],
    );

    const accountRank = accountRankResult[0][0];

    const accountDonor = {
      Gems: account.gems,
      Coins: account.coins,
      SalesPackages: [],
      UnknownSalesPackages: [],
      Transactions: [],
      CoinRewards: []
    };

    const accountPunishmentsResult = await connection.query(
      'SELECT * FROM accountpunishments WHERE target=?', [account.name],
    );

    const accountPunishments = [];

    accountPunishmentsResult[0].forEach(punishment => {
      accountPunishments.push({
        PunishmentId: punishment.id,
        Admin: punishment.admin,
        Sentence: punishment.sentence,
        Reason: punishment.reason,
        Duration: punishment.duration,
        Admin: punishment.admin,
        Severity: punishment.severity
      });
    });

    console.log(accountPunishments)

    connection.release();
    //Punishment
    return {
      AccountId: account.id,
      LastLogin: new Date().getTime(),
      Name: account.name,
      Rank: accountRank.rankIdentifier,
      DonorToken: accountDonor,
      Time: 0,
      Punishments: accountPunishments
    };
  });

fastify.post('/PlayerAccount/Punish', {
  schema: {
    body: {
      type: 'object',
      properties: {
        Target: { type: 'string' },
        Category: { type: 'string' },
        Sentence: { type: 'string' },
        Reason: { type: 'string' },
        Duration: { type: 'integer' },
        Admin: { type: 'string' },
        Severity: { type: 'integer' },
      }
    }
  }
}, async (request, reply) => {
  const connection = await fastify.mysql.getConnection()
  const punishment = request.body;
  connection.query('INSERT INTO accountpunishments(target, category, sentence, reason, duration, admin, severity) VALUES ("' + punishment.Target + '", "' + punishment.Category + '", "' + punishment.Sentence + '", "' + punishment.Reason + '", "' + punishment.Duration + '", "' + punishment.Admin + '", "' + punishment.Severity + '")');
  connection.release();
  return 'Punished'; //I'm lazy af lmao
});


fastify.post('/PlayerAccount/RemovePunishment', {
  schema: {
    body: {
      type: 'object',
      properties: {
        PunishmentId: { type: 'integer' },
        Target: { type: 'string' },
        Reason: { type: 'string' },
        Admin: { type: 'string' },
      }
    }
  }
}, async (request, reply) => {
  console.log("RemovePunishment called")
  console.log(request.body)
});

fastify.post('/PlayerAccount/GemReward', {
  schema: {
    body: {
      type: 'object',
      properties: {
        Source: { type: 'string' },
        Name: { type: 'string' },
        Amount: { type: 'integer' },
      }
    }
  }
}, async (request, reply) => {
  console.log("GemReward called")
  console.log(request.body)
});

fastify.post('/PlayerAccount/CoinReward', {
  schema: {
    body: {
      type: 'object',
      properties: {
        Source: { type: 'string' },
        Name: { type: 'string' },
        Amount: { type: 'integer' },
      }
    }
  }
}, async (request, reply) => {
  console.log("CoinReward called")
  console.log(request.body)
});

fastify.post('/PlayerAccount/PurchaseKnownSalesPackage', {
  schema: {
    body: {
      type: 'object',
      properties: {
        AccountName: { type: 'string' },
        UsingCredits: { type: 'boolean' },
        SalesPackageId: { type: 'integer' },
      }
    }
  }
}, async (request, reply) => {
  console.log("CoinReward called")
  console.log(request.body)
});

//Dummy
fastify.post('/Dominate/GetSkills', async (request, reply) => {
  return [];
});

fastify.post('/PlayerAccount/GetPunishClient', {
  schema: {
    body: {
      type: 'string'
    }
  }
}, async (request, reply) => {
  const connection = await fastify.mysql.getConnection()
  const name = request.body;
  const accountPunishmentsResult = await connection.query(
    'SELECT * FROM accountpunishments WHERE target=?', [name],
  );
  const accountPunishments = [];
  accountPunishmentsResult[0].forEach(punishment => {
    accountPunishments.push({
      PunishmentId: punishment.id,
      Admin: punishment.admin,
      Sentence: punishment.sentence,
      Reason: punishment.reason,
      Duration: punishment.duration,
      Admin: punishment.admin,
      Severity: punishment.severity
    });
  });
  connection.release();
  return {
    Name: name,
    Time: 0,
    Punishments: accountPunishments
  };
});

fastify.post('/PlayerAccount/purchaseUnknownSalesPackage', {
  schema: {
    body: {
      type: 'object',
      properties: {
        AccountName: { type: 'string' },
        SalesPackageName: { type: 'string' },
        CoinPurchase: { type: 'string' },
        Cost: { type: 'integer' },
        Premium: { type: 'boolean' },
      }
    }
  }
}, async (request, reply) => {
  console.log("CoinReward called")
  console.log(request.body)
});


//Booster for arcade group
fastify.get('/arcade', async (request, reply) => {
  console.log("Getting boosters called")
  return []; //No booster for u haha
});

fastify.get('/booster', async (request, reply) => {
  console.log("Getting boosters called")
  return []; //No booster for u haha
});


//Run the server!
const start = async () => {
  try {
    await fastify.listen(1000)
  } catch (err) {
    fastify.log.error(err)
    process.exit(1)
  }
}
start();