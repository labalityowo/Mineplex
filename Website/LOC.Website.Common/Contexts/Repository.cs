namespace LOC.Core.Data
{
    using System;
    using System.Data;
    using System.Data.Entity;
    using System.Data.Entity.Infrastructure;
    using System.Data.Entity.Validation;
    using System.Linq;
    using System.Linq.Expressions;
    using LOC.Website.Common;

    public class Repository<TContext> : IRepository
        where TContext : DbContext
    {
        public Repository(TContext context)
        {
            Context = context;
        }

        public DbContext Context { get; private set; }

        public virtual TEntity Add<TEntity>(TEntity entity) where TEntity : class
        {
            return Context.Set<TEntity>().Add(entity);
        }

        public bool Any<TEntity>(Expression<Func<TEntity, bool>> predicate) where TEntity : class
        {
            return Context.Set<TEntity>().AsQueryable().Any(predicate);
        }

        public bool Any<TEntity>() where TEntity : class
        {
            return Context.Set<TEntity>().Any();
        }

        public void AddOrEdit<TEntity>(TEntity entity) where TEntity : class
        {
            var tracked = Context.Set<TEntity>().Find(Context.KeyValuesFor(entity));
            if (tracked != null)
            {
                Context.Entry(tracked).CurrentValues.SetValues(entity);
            }
            else
            {
                Context.Set<TEntity>().Add(entity);
            }
        }

        public void CommitChanges()
        {
            const string PREFIX = "CommitChanges() failed because:  ";
            try
            {
                if (Context.SaveChanges() == 0)
                    Log("WARNING", "No entities to save: " + Environment.StackTrace);
                    /*
                     * List<Object> modifiedOrAddedEntities = context.ChangeTracker.Entries()
 .Where(x => x.State == System.Data.EntityState.Modified 
        || x.State == System.Data.EntityState.Added)
 .Select(x=>x.Entity).ToList();
                     * */
            }
            catch (DbEntityValidationException ex)
            {
                var l = (from err in ex.EntityValidationErrors from ve in err.ValidationErrors select ve.ErrorMessage).ToList();
                Log("ERROR", PREFIX + String.Join("; ", l));
                throw new ApplicationException(PREFIX + String.Join("; ", l), ex);  // contains the human-readable validation exception
            }
            catch (DbUpdateException e)
            {
                if (e.InnerException != null && e.InnerException.InnerException != null && !String.IsNullOrEmpty(e.InnerException.InnerException.Message))
                {
                    Log("ERROR", PREFIX + String.Join("; ", e.InnerException.InnerException.Message));
                    throw new ApplicationException(PREFIX + String.Join("; ", e.InnerException.InnerException.Message), e);  // contains the reason
                }
                throw;
            }
            catch (Exception e)
            {
                Log("ERROR", PREFIX + String.Join("; ", e.InnerException.InnerException.Message));
            }
        }

        public virtual void Delete<TEntity>(TEntity entity) where TEntity : class
        {
            Context.Set<TEntity>().Attach(entity);
            Context.Set<TEntity>().Remove(entity);
        }

        public virtual void Edit<TEntity>(TEntity entity) where TEntity : class
        {
            Context.Entry(entity).State = EntityState.Modified;
        }

        public virtual void Attach<TEntity>(TEntity entity) where TEntity : class
        {
            Context.Set<TEntity>().Attach(entity);
        }

        public virtual TEntity GetByKeyValues<TEntity>(params object[] keyValues) where TEntity : class
        {
            var entity = Context.Set<TEntity>().Find(keyValues);
            entity.LoadNavigationProperties(Context);
            return entity;
        }

        public IQueryable<TEntity> Where<TEntity>(Expression<Func<TEntity, bool>> predicate) where TEntity : class
        {
            var query = Context.Set<TEntity>().AsQueryable().Where(predicate);
            return query;
        }

        public IQueryable<TEntity> GetAll<TEntity>() where TEntity : class
        {
            var query = Context.Set<TEntity>().AsQueryable();
            return query;
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        private void Dispose(bool disposing)
        {
            if (disposing)
            {
                var context = Context;
                Context = null;
                if (context != null)
                {
                    context.Dispose();
                }
            }
        }

        private void Log(string category, string message)
        {
            Add(new LogEntry
            {
                Date = DateTime.Now,
                Category = category,
                Message = message
            });
            CommitChanges();
        }
    }
}

