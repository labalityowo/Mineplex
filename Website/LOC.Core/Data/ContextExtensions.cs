namespace LOC.Core.Data
{
    using System;
    using System.Collections.Generic;
    using System.Data.Entity;
    using System.Data.Entity.Infrastructure;
    using System.Data.Metadata.Edm;
    using System.Data.Objects;
    using System.Linq;
    using System.Linq.Expressions;
    using System.Text;
    using System.Text.RegularExpressions;

    public static class ContextExtensions
    {
        private const string INDITIFY_OBJECT = "SELECT * FROM sys.indexes where name='{0}'";

        public static object[] KeyValuesFor<TEntity>(this DbContext context, TEntity entity) where TEntity : class
        {
            var entry = context.Entry(entity);
            return context.KeysFor(entity.GetType()).Select(x => entry.Property(x).CurrentValue).ToArray();
        }

        public static IQueryable<TEntity> LoadNavigationProperties<TEntity>(this IQueryable<TEntity> query, DbContext context)
            where TEntity : class
        {
            var entityType = GetEntityType(context, typeof(TEntity));
            if (entityType == null)
            {
                throw new ArgumentException(string.Format("The type '{0}' is not mapped as an entity type.", entityType.Name));
            }

            return entityType.NavigationProperties.Aggregate(
                                                             query,
                                                             (current, navigationProperty) => current.Include(navigationProperty.Name));
        }

        public static void LoadNavigationProperties<TEntity>(this TEntity entity, DbContext context) where TEntity : class
        {
            if (entity == null)
            {
                return;
            }

            var objectContext = ((IObjectContextAdapter)context).ObjectContext;
            var items = objectContext.MetadataWorkspace.GetItems<EntityType>(DataSpace.OSpace);
            var item = items == null
                           ? null
                           : items.FirstOrDefault(x => x.Name.Equals(ObjectContext.GetObjectType(entity.GetType()).Name));
            var navigationPropertyNames = item == null ? new List<string>() : item.NavigationProperties.Select(x => x.Name);
            foreach (var prop in navigationPropertyNames)
            {
                objectContext.LoadProperty(entity, prop);
            }
        }

        public static void CreateIndex<T>(this DbContext context, Expression<Func<T, object>> expression, bool isUnique) where T : class
        {
            var database = context.Database;
            if (database == null)
            {
                throw new ArgumentNullException("context", @"Database is null");
            }

            var realtableName = GetTableName<T>(context);
            var tablename = typeof(T).Name;
            var columnName = GetColumnExpressionName(expression.Body);
            var indexColumnName = GetColumnName(expression.Body);
            var indexName = string.Format("IX_{0}_{1}", tablename, columnName);
            var createIndexSql = string.Format(
                                               isUnique ? "CREATE UNIQUE INDEX {0} ON {1} ({2})" : "CREATE INDEX {0} ON {1} ({2})",
                                               indexName,
                                               realtableName,
                                               indexColumnName);
            //TODO Check index?
            var checkIndex = string.Format(INDITIFY_OBJECT, indexName);
            var result = database.SqlQuery<object>(checkIndex).Count();
            if (result <= 0)
            {
                //TODO Create index
                database.ExecuteSqlCommand(createIndexSql);
            }
        }

        private static string GetColumnExpressionName(Expression expression)
        {
            var memberExps = expression as NewExpression;

            if (memberExps != null)
            {
                var sb = new StringBuilder();

                foreach (var memberExp in memberExps.Arguments)
                {
                    var member = memberExp as MemberExpression;
                    if (memberExp == null
                        || member == null)
                    {
                        throw new ArgumentException(@"Cannot get name from expression", "expression");
                    }

                    sb.Append(member.Member.Name);
                    sb.Append("_");
                }
                return sb.ToString().Substring(0, sb.ToString().Length - 1);
            }
            return string.Empty;
        }

        private static string GetColumnName(Expression expression)
        {
            var memberExps = expression as NewExpression;

            if (memberExps != null)
            {
                var sb = new StringBuilder();

                foreach (var memberExp in memberExps.Arguments)
                {
                    var member = memberExp as MemberExpression;
                    if (memberExp == null
                        || member == null)
                    {
                        throw new ArgumentException(@"Cannot get name from expression", "expression");
                    }

                    sb.Append(member.Member.Name);
                    sb.Append(",");
                }
                return sb.ToString().Substring(0, sb.ToString().Length - 1);
            }
            return string.Empty;
        }

        private static EntityType GetEntityType(IObjectContextAdapter context, Type entityType)
        {
            entityType = ObjectContext.GetObjectType(entityType);

            var workspace = context.ObjectContext.MetadataWorkspace;
            var itemCollection = (ObjectItemCollection)workspace.GetItemCollection(DataSpace.OSpace);

            EntityType type;
            if (workspace == null)
            {
                type = null;
            }
            else
            {
                var types = workspace.GetItems<EntityType>(DataSpace.OSpace);
                type = types == null ? null : types.SingleOrDefault(t => itemCollection.GetClrType(t) == entityType);
            }
            return type;
        }

        private static string GetTableName<T>(IObjectContextAdapter context) where T : class
        {
            var objectContext = context.ObjectContext;
            return GetTableName<T>(objectContext);
        }

        private static string GetTableName<T>(ObjectContext context) where T : class
        {
            var sql = context.CreateObjectSet<T>().ToTraceString();
            var regex = new Regex("FROM (?<table>.*) AS");
            var match = regex.Match(sql);
            var table = match.Groups["table"].Value;
            return table;
        }

        private static IEnumerable<string> KeysFor(this IObjectContextAdapter context, Type entityType)
        {
            var type = GetEntityType(context, entityType);

            if (type == null)
            {
                throw new ArgumentException(
                    string.Format("The type '{0}' is not mapped as an entity type.", entityType.Name), "entityType");
            }

            return type.KeyMembers.Select(k => k.Name);
        }
    }
}
